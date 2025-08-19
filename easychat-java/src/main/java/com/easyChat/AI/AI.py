import ollama
import sys
import io
# 设置标准输出的编码为 UTF-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

if __name__ == "__main__":

    msg_content = sys.argv[1]
    client = ollama.Client(host='http://localhost:11434')
    print('----------')
    models = client.list()
    print('Available models:', models)

    print('----------')
    response = client.generate(
        model="deepseek-r1:1.5b",  # 指定模型名称
        prompt=msg_content
    )
    print(response['response'])