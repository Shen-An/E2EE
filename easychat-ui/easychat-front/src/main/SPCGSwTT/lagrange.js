const Decimal = require('decimal.js');
const readline = require('readline');

Decimal.set({ precision: 80, rounding: 4 });

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

// 椭圆曲线和双线性映射的简化实现
class EllipticCurve {
    constructor() {
        // 使用简化的椭圆曲线参数 y² = x³ + ax + b
        this.a = new Decimal(0);
        this.b = new Decimal(7); // 简化的secp256k1参数
        this.p = new Decimal('115792089237316195423570985008687907853269984665640564039457584007908834671663'); // 大质数
        this.zeroPoint = { x: new Decimal(0), y: new Decimal(0) }; // 加法单位元（零元素）
    }

    // 点加法
    add(p1, p2) {
        // 处理零元素情况
        if (!p1 || (p1.x.isZero() && p1.y.isZero())) return p2;
        if (!p2 || (p2.x.isZero() && p2.y.isZero())) return p1;
    
        // 简化实现
        let slope;
        if (p1.x.equals(p2.x)) {
            // 点加倍或相反点
            if (p1.y.equals(p2.y.neg().mod(this.p))) {
                return this.zeroPoint; // 相反点相加得到零元素
            }
            slope = p1.x.pow(2).mul(3).add(this.a).div(p1.y.mul(2)).mod(this.p);
        } else {
            slope = p2.y.sub(p1.y).div(p2.x.sub(p1.x)).mod(this.p);
        }
    
        const x3 = slope.pow(2).sub(p1.x).sub(p2.x).mod(this.p);
        const y3 = slope.mul(p1.x.sub(x3)).sub(p1.y).mod(this.p);
    
        return { x: x3, y: y3 };
    }
    
    // 点乘法（倍加法）
    multiply(point, scalar) {
        if (!point || (point.x.isZero() && point.y.isZero())) {
            return this.zeroPoint;
        }
    
        let result = this.zeroPoint; // 使用零元素作为初始值
        let current = { ...point };
        let s = new Decimal(scalar).mod(this.p); // 确保标量在有限域内
    
        while (s.gt(0)) {
            if (s.mod(2).equals(1)) {
                result = this.add(result, current);
            }
            current = this.add(current, current);
            s = s.div(2).floor();
        }
    
        return result;
    }
    
    // 双线性映射
    pairing(p1, p2) {
        return this.multiply(p2, p1.x);
    }

}

// KZG多项式承诺方案
class KZG {
    constructor() {
        this.curve = new EllipticCurve();
        // 生成随机生成元（使用secp256k1的生成元）
        this.G = {
            x: new Decimal('55066263022277343669578718895168534326250603453777594175500187360389116729240'),
            y: new Decimal('32670510020758816978083085130507043184471273380659243275938904335757337482424')
        };
        this.H = {
            x: new Decimal('883423532389192164791648750360308885314476597252960362792450860609699839'),
            y: new Decimal('11342371305586404523178723960318038105974174689514320076928659388720039155')
        };

        // 生成秘密参数s（仅 prover 知道）
        this.s = new Decimal(Math.random() * 1000000).floor();
    
        // 生成公开参数 [G, sG, s²G, ..., s^dG]
        this.d = 10;
        this.setup = [this.G];
        for (let i = 1; i <= this.d; i++) {
            this.setup.push(this.curve.multiply(this.G, this.s.pow(i)));
        }
    }
    
    // 生成多项式承诺
    commit(coefficients) {
        let commitment = this.curve.zeroPoint;
        for (let i = 0; i < coefficients.length; i++) {
            if (i >= this.setup.length) break;
            const term = this.curve.multiply(this.setup[i], coefficients[i]);
            commitment = this.curve.add(commitment, term);
        }
        return commitment;
    }
    
    // 生成评估证明
    prove(coefficients, x) {
        const n = coefficients.length - 1;
        if (n <= 0) {
            // 常数多项式的特殊情况处理
            return this.curve.zeroPoint;
        }
    
        // 计算商多项式 Q(x) = (f(x) - f(z)) / (x - z)
        const fx = this.evaluate(coefficients, x);
        const shiftedCoeffs = [...coefficients];
        shiftedCoeffs[0] = shiftedCoeffs[0].sub(fx);
    
        const qCoeffs = this.divideByLinear(shiftedCoeffs, x);
        return this.commit(qCoeffs);
    }
    
    // 验证评估证明
    verify(commitment, x, y, proof) {
        // 检查 e(proof, xG - H) == e(commitment - yG, H)
        const xG = this.curve.multiply(this.G, x);
        const xGMinusH = this.curve.add(xG, {
            x: this.H.x.neg().mod(this.curve.p),
            y: this.H.y.neg().mod(this.curve.p)
        });
    
        const yG = this.curve.multiply(this.G, y);
        const commitMinusYG = this.curve.add(commitment, {
            x: yG.x.neg().mod(this.curve.p),
            y: yG.y.neg().mod(this.curve.p)
        });
    
        const left = this.curve.pairing(proof, xGMinusH);
        const right = this.curve.pairing(commitMinusYG, this.H);
    
        // 检查配对结果是否相等
        return left.x.mod(this.curve.p).equals(right.x.mod(this.curve.p)) &&
            left.y.mod(this.curve.p).equals(right.y.mod(this.curve.p));
    }
    
    // 计算多项式在x处的值
    evaluate(coefficients, x) {
        let result = new Decimal(0);
        for (let i = 0; i < coefficients.length; i++) {
            result = result.add(coefficients[i].mul(x.pow(i)));
        }
        return result;
    }
    
    // 多项式除以线性项 (x - a)
    divideByLinear(coefficients, a) {
        const n = coefficients.length - 1;
        if (n === 0) return [];
    
        const q = new Array(n).fill(new Decimal(0));
    
        q[n - 1] = coefficients[n];
        for (let i = n - 2; i >= 0; i--) {
            q[i] = coefficients[i + 1].add(q[i + 1].mul(a));
        }
    
        return q;
    }

}

/**

 * 生成随机多项式系数

 * @param {number} degree 多项式的度数

 * @returns {Array} 多项式系数数组 [a0, a1, a2]
   */
   function generatePolynomial(degree = 2) {
   const coefficients = [];
   console.log('\n=== 随机生成多项式 ===');

   for (let i = 0; i <= degree; i++) {
       // 生成-10到10之间的随机系数
       const coeff = new Decimal((Math.random() * 20 - 10).toFixed(6));
       coefficients.push(coeff);
   }

   // 显示生成的多项式
   let polynomialStr = 'f(x) = ';
   for (let i = 0; i < coefficients.length; i++) {
       if (i === 0) {
           polynomialStr += coefficients[i].toString();
       } else {
           const sign = coefficients[i].isNegative() ? ' - ' : ' + ';
           const absCoeff = coefficients[i].abs().toString();
           if (i === 1) {
               polynomialStr += sign + absCoeff + 'x';
           } else {
               polynomialStr += sign + absCoeff + 'x^' + i;
           }
       }
   }

   console.log('生成的多项式：', polynomialStr);
   console.log('系数：', coefficients.map(c => c.toString()));

   return coefficients;
   }

/**

 * 根据系数和x坐标计算y坐标

 * @param {Array} coefficients 多项式系数数组

 * @param {Decimal} x x坐标值

 * @returns {Decimal} 对应的y坐标值
   */
   function calculateY(coefficients, x) {
   let y = new Decimal(0);

   for (let i = 0; i < coefficients.length; i++) {
       // y += coefficients[i] * x^i
       const term = coefficients[i].mul(x.pow(i));
       y = y.add(term);
   }

   return y;
   }

/**

 * 拉格朗日插值计算多项式系数

 * @param {Array} points 点数组 [{x: Decimal, y: Decimal}, ...]

 * @returns {Array} 插值多项式的系数
   */
   function lagrangeInterpolation(points) {
   const n = points.length;
   const coefficients = new Array(n).fill(0).map(() => new Decimal(0));

   for (let i = 0; i < n; i++) {
       // 计算拉格朗日基函数 L_i(x)
       let numerator = [new Decimal(1)];
       let denominator = new Decimal(1);

       for (let j = 0; j < n; j++) {
           if (i !== j) {
               // 分子乘以 (x - x_j)
               const newNumerator = new Array(numerator.length + 1).fill(0).map(() => new Decimal(0));
               for (let k = 0; k < numerator.length; k++) {
                   newNumerator[k] = newNumerator[k].add(numerator[k].mul(points[j].x.neg()));
                   newNumerator[k + 1] = newNumerator[k + 1].add(numerator[k]);
               }
               numerator = newNumerator;
       
               // 分母乘以 (x_i - x_j)
               denominator = denominator.mul(points[i].x.sub(points[j].x));
           }
       }
       
       // 将 y_i * L_i(x) 加到结果多项式中
       for (let k = 0; k < numerator.length && k < coefficients.length; k++) {
           const term = points[i].y.mul(numerator[k]).div(denominator);
           coefficients[k] = coefficients[k].add(term);
       }

   }

   return coefficients;
   }

/**

 * 计算f(0)的值
 * @param {Array} coefficients 多项式系数数组
 * @returns {Decimal} f(0)的值（即常数项）
   */
   function calculateF0(coefficients) {
   // f(0) = a0（常数项）
   return coefficients[0] || new Decimal(0);
   }

/**

 * 获取用户输入的x坐标
 * @param {number} index 点的索引
 * @returns {Promise<Decimal>} x坐标值
   */
   function getXCoordinate(index) {
   return new Promise((resolve) => {
       rl.question(`请输入第${index + 1}个点的x坐标: `, (input) => {
           try {
               const x = new Decimal(input.trim());
               resolve(x);
           } catch (error) {
               console.log('输入格式错误，请输入有效数字');
               resolve(getXCoordinate(index));
           }
       });
   });
   }

/**

 * 主函数
   */
   async function main() {
   console.log('=== 带KZG零知识证明的拉格朗日插值计算器 ===\n');

   try {
       // 初始化KZG
       const kzg = new KZG();

       // 1. 随机生成二次多项式
       const originalCoefficients = generatePolynomial(2);
       
       // 2. 生成多项式承诺
       const commitment = kzg.commit(originalCoefficients);
       console.log('\n=== KZG多项式承诺 ===');
       console.log(`承诺值 (x): ${commitment.x.toString().substring(0, 20)}...`);
       console.log(`承诺值 (y): ${commitment.y.toString().substring(0, 20)}...`);
       
       // 3. 获取用户输入的三个x坐标
       console.log('\n=== 请输入三个点的x坐标 ===');
       const points = [];
       
       for (let i = 0; i < 3; i++) {
           const x = await getXCoordinate(i);
           const y = calculateY(originalCoefficients, x);
           points.push({ x, y });
           console.log(`点${i + 1}: (${x.toString()}, ${y.toString()})`);
       }
       
       // 4. 使用拉格朗日插值计算多项式系数
       console.log('\n=== 拉格朗日插值结果 ===');
       const interpolatedCoefficients = lagrangeInterpolation(points);
       
       console.log('插值多项式系数：');
       interpolatedCoefficients.forEach((coeff, index) => {
           console.log(`a${index} = ${coeff.toString()}`);
       });
       
       // 5. 计算并输出f(0)
       const f0_original = calculateF0(originalCoefficients);
       const f0_interpolated = calculateF0(interpolatedCoefficients);
       
       console.log('\n=== f(0)计算结果 ===');
       console.log(`原始多项式 f(0) = ${f0_original.toString()}`);
       console.log(`插值多项式 f(0) = ${f0_interpolated.toString()}`);
       console.log(`插值误差 = ${f0_original.sub(f0_interpolated).abs().toString()}`);
       
       // 6. 使用KZG进行零知识证明
       console.log('\n=== KZG零知识证明验证 ===');
       const z = new Decimal(0);
       const y = f0_original;
       
       // 生成证明
       const proof = kzg.prove(originalCoefficients, z);
       console.log('已生成证明');
       
       // 验证证明
       const isValid = kzg.verify(commitment, z, y, proof);
       console.log(`证明验证结果: ${isValid ? '有效' : '无效'}`);
       
       // 验证插值多项式的证明
       const interpolatedCommitment = kzg.commit(interpolatedCoefficients);
       const interpolatedProof = kzg.prove(interpolatedCoefficients, z);
       const isInterpolatedValid = kzg.verify(interpolatedCommitment, z, f0_interpolated, interpolatedProof);
       console.log(`插值多项式证明验证结果: ${isInterpolatedValid ? '有效' : '无效'}`);

   } catch (error) {
       console.error('程序执行出错：', error.message);
       console.error('错误堆栈：', error.stack);
   } finally {
       rl.close();
   }
   }

// // 运行主程序
// if (require.main === module) {5
//     main();
// }

export {
    generatePolynomial,
    calculateY,
    calculateF0,
    lagrangeInterpolation,
    KZG
};