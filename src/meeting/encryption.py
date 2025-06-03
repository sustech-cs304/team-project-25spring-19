# encryption.py

import os
import base64

class FeistelCipher:
    def __init__(self, key, rounds=16):
        """
        初始化 Feistel 加密器。

        :param key: 密钥（字符串）。
        :param rounds: Feistel 网络的轮数。
        """
        self.key = self._process_key(key)
        self.rounds = rounds

    def _process_key(self, key):
        """
        处理密钥，将其转换为整数列表。

        :param key: 密钥（字符串）。
        :return: 密钥整数列表。
        """
        key_bytes = key.encode('utf-8')
        key_int = [b for b in key_bytes]
        return key_int

    def _pad(self, data):
        """
        使用 PKCS7 填充数据。

        :param data: 数据（字节）。
        :return: 填充后的数据（字节）。
        """
        pad_len = 8 - (len(data) % 8)
        padding = bytes([pad_len] * pad_len)
        return data + padding

    def _unpad(self, data):
        """
        移除 PKCS7 填充。

        :param data: 填充后的数据（字节）。
        :return: 原始数据（字节）。
        """
        pad_len = data[-1]
        if pad_len < 1 or pad_len > 8:
            raise ValueError("Invalid padding.")
        return data[:-pad_len]

    def _round_function(self, right, round_key):
        """
        Feistel 网络的轮函数（简单的异或操作）。

        :param right: 右半部分数据（整数）。
        :param round_key: 轮密钥（整数）。
        :return: 轮函数输出（整数）。
        """
        return right ^ round_key

    def encrypt_block(self, block):
        """
        加密一个块。

        :param block: 8字节块（字节）。
        :return: 加密后的块（字节）。
        """
        if len(block) != 8:
            raise ValueError("Block size must be 8 bytes.")

        # 将块分为左右两部分
        left = int.from_bytes(block[:4], byteorder='big')
        right = int.from_bytes(block[4:], byteorder='big')

        for i in range(self.rounds):
            round_key = self.key[i % len(self.key)]
            temp = left ^ self._round_function(right, round_key)
            left, right = right, temp

        # 合并左右两部分
        encrypted_block = right.to_bytes(4, byteorder='big') + left.to_bytes(4, byteorder='big')
        return encrypted_block

    def decrypt_block(self, block):
        """
        解密一个块。

        :param block: 加密后的8字节块（字节）。
        :return: 解密后的块（字节）。
        """
        if len(block) != 8:
            raise ValueError("Block size must be 8 bytes.")

        # 将块分为左右两部分
        right = int.from_bytes(block[:4], byteorder='big')
        left = int.from_bytes(block[4:], byteorder='big')

        for i in reversed(range(self.rounds)):
            round_key = self.key[i % len(self.key)]
            temp = right ^ self._round_function(left, round_key)
            right, left = left, temp

        # 合并左右两部分
        decrypted_block = left.to_bytes(4, byteorder='big') + right.to_bytes(4, byteorder='big')
        return decrypted_block

    def encrypt(self, plaintext):
        """
        加密明文。

        :param plaintext: 明文字符串。
        :return: 加密后的字符串（Base64编码）。
        """
        plaintext_bytes = plaintext.encode('utf-8')
        padded = self._pad(plaintext_bytes)
        ciphertext = b''

        # 分块加密
        for i in range(0, len(padded), 8):
            block = padded[i:i+8]
            encrypted_block = self.encrypt_block(block)
            ciphertext += encrypted_block

        # 返回 Base64 编码的密文
        return base64.b64encode(ciphertext).decode('utf-8')

    def decrypt(self, ciphertext):
        """
        解密密文。

        :param ciphertext: 加密后的字符串（Base64编码）。
        :return: 解密后的明文字符串。
        """
        ciphertext_bytes = base64.b64decode(ciphertext)
        if len(ciphertext_bytes) % 8 != 0:
            raise ValueError("Invalid ciphertext length.")

        plaintext_padded = b''

        # 分块解密
        for i in range(0, len(ciphertext_bytes), 8):
            block = ciphertext_bytes[i:i+8]
            decrypted_block = self.decrypt_block(block)
            plaintext_padded += decrypted_block

        # 移除填充
        plaintext_bytes = self._unpad(plaintext_padded)
        return plaintext_bytes.decode('utf-8')

def load_key(path='secret.key'):
    """
    从文件加载密钥。

    :param path: 密钥文件路径。
    :return: 密钥字符串。
    """
    if not os.path.exists(path):
        raise FileNotFoundError("密钥文件不存在，请先生成密钥。")
    with open(path, 'r') as key_file:
        key = key_file.read().strip()
    return key

def generate_key(path='secret.key', key_length=32):
    """
    生成一个随机密钥并保存到文件。

    :param path: 密钥文件路径。
    :param key_length: 密钥长度（字符）。
    :return: 生成的密钥字符串。
    """
    # 生成由可打印字符组成的密钥
    key = ''.join([chr(os.urandom(1)[0] % 94 + 33) for _ in range(key_length)])
    with open(path, 'w') as key_file:
        key_file.write(key)
    return key
