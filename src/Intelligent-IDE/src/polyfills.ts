// 为浏览器环境中缺少的 Node.js 变量提供 polyfill

// 添加 global 对象
if (typeof window !== 'undefined') {
  (window as any).global = window;
}

// 添加 process.env 对象
if (typeof window !== 'undefined' && !(window as any).process) {
  (window as any).process = { env: {} };
}

// 添加 Buffer 对象（如果需要）
if (typeof window !== 'undefined' && !(window as any).Buffer) {
  (window as any).Buffer = {
    isBuffer: () => false
  };
}

export default {}; 