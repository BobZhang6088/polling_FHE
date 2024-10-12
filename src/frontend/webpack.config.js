const path = require('path');

module.exports = {
    entry: './src/index.js',  // 前端入口文件
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, 'dist'),
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                },
            },
            {
                test: /\.wasm$/,
                type: 'javascript/auto',
                loader: 'file-loader',
            },
        ],
    },
    experiments: {
        asyncWebAssembly: true,  // 确保 WebAssembly 支持
    },
};
