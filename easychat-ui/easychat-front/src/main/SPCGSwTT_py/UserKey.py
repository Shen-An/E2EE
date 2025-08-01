from py_ecc.bn128 import bn128_curve, multiply, add, G1, G2, eq
from hashlib import sha256
import random
import json
class SPCEParams:
    def __init__(self, n, epsilon, t):
        self.n = n
        self.epsilon = epsilon
        self.t = t  # 容错阈值
        self.group_order = bn128_curve.curve_order

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
    
    def to_dict(self):
        return {
            "t": self.t,
            "sk": self.sk,
            "pk": str(self.pk),
            "h": self.h,
            "coeffs": self.coeffs
        }

if __name__ == "__main__":
    params = SPCEParams(n=2, epsilon=0.1, t=2)
    user = UserKey(params)
    tag = sha256(str(user.pk).encode()).hexdigest()  # 用户标识

    data = {
        "user": user.to_dict(),
        "tag": tag
    }
    print(json.dumps(data))
    