import jieba
import json
from cuckoofilter import CuckooFilter
from py_ecc.bn128 import bn128_curve, multiply, add, G1, G2, FQ
from hashlib import sha256
import random
import sys
import io
# 设置标准输出的编码为 UTF-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

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

    # 修改部分：将 content 编码为 UTF - 8 字节再进行异或
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
        return int(result)

    def to_dict(self):
        return {
            "t": self.t,
            "sk": self.sk,
            "pk": str(self.pk),
            "h": self.h,
            "coeffs": [int(coeff) for coeff in self.coeffs]  # 将系数转换为整数
        }


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


def deserialize_data(A_json, T_json):
    """将 JSON 恢复为 A 和 T"""
    A_loaded = json.loads(A_json)
    A_restored = dict_to_point(A_loaded)

    T_loaded = json.loads(T_json)
    T_restored = {
        tuple(map(int, k.strip("()").split(", "))): dict_to_point(v)
        for k, v in T_loaded.items()
    }
    return A_restored, T_restored


class GroupManager:
    def __init__(self, params, D):
        self.params = params
        self.D = set(D)
        self.records = {}  # 记录用户 tag 及其非法消息次数和点集

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
            print(f"用户{tag[:8]}... 非法次数不足{self.params.t}次，当前次数：{self.records[tag]['count']}")
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


def spce_decrypt(sk, ct):
    alpha = sk
    u_i, (Q0, Q1), ct_enc = ct
    S0 = multiply(Q0, alpha)
    S1 = multiply(Q1, alpha)
    key = sha256(
        S0[0].n.to_bytes(32, 'big') +
        S0[1].n.to_bytes(32, 'big') +
        S1[0].n.to_bytes(32, 'big') +
        S1[1].n.to_bytes(32, 'big')
    ).digest()
    # 修改部分：异或后解码为 UTF - 8 字符串
    decrypted_bytes = bytes([ct_byte ^ key[i % len(key)] for i, ct_byte in enumerate(ct_enc)])
    decrypted = decrypted_bytes.decode('utf-8', errors='ignore')
    return u_i, decrypted


# 反序列化密文
def deserialize_ct(ct_json):
    ct_data = json.loads(ct_json)
    u_i = ct_data["u_i"]
    Q0 = dict_to_point(ct_data["Q0"])
    Q1 = dict_to_point(ct_data["Q1"])
    ct_enc = bytes.fromhex(ct_data["ct_enc"])
    return (u_i, (Q0, Q1), ct_enc)


# 从文件读取 alpha
def load_alpha(file_path):
    with open(file_path, 'r') as f:
        return json.load(f)


# 从文件读取 A, T, gm
def load_data_from_file(file_path):
    with open(file_path, 'r') as f:
        lines = f.readlines()

    A_json = None
    T_json = None
    gm_json = None

    for line in lines:
        if line.startswith("Serialized A:"):
            A_json = line.split("Serialized A: ")[1].strip()
        elif line.startswith("Serialized T:"):
            T_json = line.split("Serialized T: ")[1].strip()
        elif line.startswith("Serialized GroupManager:"):
            gm_json = line.split("Serialized GroupManager: ")[1].strip()

    A, T = deserialize_data(A_json, T_json)
    gm = deserialize_gm(gm_json)

    return A, T, gm


# 🎯 主程序
if __name__ == "__main__":
    ui = sys.argv[1]
    ui = json.loads(ui)
    # print(ui)
    ui = [int(num) for num in ui]    

    user_id = sys.argv[2]
    user_id = json.loads(user_id)
    # print(user_id)
    params = SPCEParams(n=2, epsilon=0.1, t=2)


    userDir = 'D:'
    saveDir = userDir + "\\.easyChat\\fileStorage\\keys\\"
    with open(saveDir + user_id + '_SPCE.json', 'r') as file:
        data = json.load(file)
    user_dict = data["user"]
    user = UserKey(params)
    user.t = user_dict["t"]
    user.sk = user_dict["sk"]
    user.pk = multiply(G1, user.sk)
    user.h = user_dict["h"]
    user.coeffs = [int(coeff) for coeff in user_dict["coeffs"]]

    vi = []
    for u_i in ui:
        v_i = user.evaluate(u_i)    
        vi.append(v_i)
    print(vi)
      
       
