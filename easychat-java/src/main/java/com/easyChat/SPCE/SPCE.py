import jieba
import json
from py_ecc.bn128 import bn128_curve, multiply, add, G1, G2, FQ
from hashlib import sha256
import random
import os

class SPCEParams:
    def __init__(self, n, epsilon, t):
        self.n = n
        self.epsilon = epsilon
        self.t = t  # 容错阈值
        self.group_order = bn128_curve.curve_order


def hash_to_g1(seed, msg):
    h = sha256(seed + msg).digest()
    x = int.from_bytes(h, 'big') % bn128_curve.curve_order
    return multiply(G1, x)


def spce_encrypt(pk, user, content):
    A, T = pk
    u_i = random.randint(1, params.group_order - 1)
    v_i = user.evaluate(u_i)
    v_i_bytes = v_i.to_bytes(32, 'big')

    h0_point = hash_to_g1(b'h0_', v_i_bytes)
    h1_point = hash_to_g1(b'h1_', v_i_bytes)
    key0 = (h0_point[0].n, h0_point[1].n)
    key1 = (h1_point[0].n, h1_point[1].n)

    beta0 = random.randint(1, params.group_order - 1)
    gamma0 = random.randint(1, params.group_order - 1)
    Q0 = add(multiply(G1, beta0), multiply(h0_point, gamma0))
    S0 = add(multiply(A, beta0), multiply(T.get(key0, G1), gamma0))

    beta1 = random.randint(1, params.group_order - 1)
    gamma1 = random.randint(1, params.group_order - 1)
    Q1 = add(multiply(G1, beta1), multiply(h1_point, gamma1))
    S1 = add(multiply(A, beta1), multiply(T.get(key1, G1), gamma1))

    if random.random() < 0.5:
        Q_pair, S_pair = (Q0, Q1), (S0, S1)
    else:
        Q_pair, S_pair = (Q1, Q0), (S1, S0)

    key = sha256(
        S_pair[0][0].n.to_bytes(32, 'big') +
        S_pair[0][1].n.to_bytes(32, 'big') +
        S_pair[1][0].n.to_bytes(32, 'big') +
        S_pair[1][1].n.to_bytes(32, 'big')
    ).digest()

    # 修改部分：将content编码为UTF-8字节再进行异或
    content_bytes = content.encode('utf-8')
    ct = bytes([content_byte ^ key[i % len(key)] for i, content_byte in enumerate(content_bytes)])
    return (u_i, Q_pair, ct)


class UserKey:
    def __init__(self, params):
        self.t = params.t
        self.sk = random.randint(1, params.group_order - 1)
        self.pk = multiply(G1, self.sk)
        self.h = int(sha256(str(self.pk).encode()).hexdigest(), 16) % params.group_order
        self.coeffs = [self.h] + [random.randint(1, params.group_order - 1) for _ in range(self.t - 1)]

    def evaluate(self, x):
        x_power = 1
        result = 0
        for coeff in self.coeffs:
            result = (result + coeff * x_power) % bn128_curve.curve_order
            x_power = (x_power * x) % bn128_curve.curve_order
        return result


def spce_gen(params, D):
    alpha = random.randint(1, params.group_order - 1)
    A = multiply(G1, alpha)
    T = {}
    for x in D:
        h0_point = hash_to_g1(b'h0_', x.encode())
        h1_point = hash_to_g1(b'h1_', x.encode())
        key0 = (h0_point[0].n, h0_point[1].n)
        key1 = (h1_point[0].n, h1_point[1].n)
        T[key0] = multiply(h0_point, alpha)
        T[key1] = multiply(h1_point, alpha)
    return (A, T), alpha


# ✅ 将 G1 点转换为 JSON 友好的格式
def point_to_dict(point):
    """将 G1 点转换为 JSON 格式"""
    return {"x": str(point[0].n), "y": str(point[1].n)}


# ✅ 将 JSON 格式还原为 G1 点
def dict_to_point(d):
    """从 JSON 还原 G1 点"""
    return (FQ(int(d["x"])), FQ(int(d["y"])))


# ✅ 序列化 A 和 T 为 JSON
def serialize_data(A, T):
    """将 A 和 T 序列化为 JSON"""
    A_json = json.dumps(point_to_dict(A))
    T_json = json.dumps({str(k): point_to_dict(v) for k, v in T.items()})
    return A_json, T_json


# ✅ 反序列化 A 和 T
def deserialize_data(A_json, T_json):
    """将 JSON 恢复为 A 和 T"""
    A_loaded = json.loads(A_json)
    A_restored = dict_to_point(A_loaded)

    T_loaded = json.loads(T_json)
    T_restored = {eval(k): dict_to_point(v) for k, v in T_loaded.items()}
    return A_restored, T_restored


class GroupManager:
    def __init__(self, params, D):
        self.params = params
        self.D = set(D)
        self.records = {}  # 记录用户tag及其非法消息次数和点集

    def check_illegal(self, content):
        # 检查内容是否包含非法字符
        return any(char in self.D for char in content)

    def record_illegal(self, tag, u_i, v_i):
        if tag not in self.records:
            self.records[tag] = {'count': 0, 'points': []}
        self.records[tag]['count'] += 1
        self.records[tag]['points'].append((u_i, v_i))

    def try_trace(self, tag):
        if tag not in self.records:
            return None
        if self.records[tag]['count'] < self.params.t:
            print(f"用户{tag[:8]}... 非法次数不足{self.params.t}次，当前已违规次数：{self.records[tag]['count']}")
            return None
        points = self.records[tag]['points'][:self.params.t]  # 取前 t 个点
        h_recovered = lagrange_interpolation(points)
        return h_recovered


def lagrange_interpolation(points, x=0):
    prime = bn128_curve.curve_order
    result = 0
    for i in range(len(points)):
        xi, yi = points[i]
        term = yi
        for j in range(len(points)):
            if j != i:
                xj = points[j][0]
                term = term * (x - xj) * pow(xi - xj, -1, prime) % prime
        result = (result + term) % prime
    return result


# ✅ 序列化 GroupManager 数据
def serialize_gm(gm):
    """将 gm 转换为 JSON 字符串"""
    gm_data = {
        "params": {
            "n": gm.params.n,
            "epsilon": gm.params.epsilon,
            "t": gm.params.t
        },
        "D": list(gm.D),
        "records": {
            tag: {"count": rec["count"], "points": rec["points"]} for tag, rec in gm.records.items()
        }
    }
    return json.dumps(gm_data)


# ✅ 反序列化 GroupManager
def deserialize_gm(gm_json):
    """从 JSON 恢复 GroupManager 实例"""
    gm_data = json.loads(gm_json)
    params = SPCEParams(
        gm_data["params"]["n"],
        gm_data["params"]["epsilon"],
        gm_data["params"]["t"]
    )
    gm = GroupManager(params, gm_data["D"])
    gm.records = {
        tag: {"count": rec["count"], "points": rec["points"]} for tag, rec in gm_data["records"].items()
    }
    return gm


# 保存 alpha 到文件
def save_alpha(alpha, file_path):
    with open(file_path, 'w') as f:
        json.dump(alpha, f)


# 从文件读取 alpha
def load_alpha(file_path):
    if os.path.exists(file_path):
        with open(file_path, 'r') as f:
            return json.load(f)
    return None


# 🎯 主程序
if __name__ == "__main__":
    params = SPCEParams(n=2, epsilon=0.1, t=3)
    D = ['sss']

    # 生成主公钥 pk 和私钥 alpha
    (A, T), alpha = spce_gen(params, D)
    gm = GroupManager(params, D)

    # ✅ 序列化 A 和 T
    A_json, T_json = serialize_data(A, T)
    print("Serialized A:", A_json)
    print("Serialized T:", T_json)

    # ✅ 反序列化 A 和 T
    A_restored, T_restored = deserialize_data(A_json, T_json)

    # ✅ 检查恢复的数据
    assert isinstance(A_restored, tuple) and len(A_restored) == 2
    assert isinstance(T_restored, dict)

    # ✅ 序列化 GroupManager
    gm_json = serialize_gm(gm)
    print("Serialized GroupManager:", gm_json)

    # 保存 alpha 到文件
    alpha_file_path = './easychat-java/src/main/java/com/easychat/SPCE/alpha.json'
    save_alpha(alpha, alpha_file_path)

#
#     # 后续读取 alpha
#     loaded_alpha = load_alpha(alpha_file_path)
#     if loaded_alpha is not None:
#         print(f"从文件中读取到的 Alpha: {loaded_alpha}")
#     else:
#         print("未能从文件中读取到 Alpha")
