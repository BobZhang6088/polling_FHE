FROM gcc:11 as builder

# 安装 CMake 和其他依赖
RUN apt-get update && apt-get install -y cmake wget

# 安装 Microsoft SEAL
RUN git clone https://github.com/microsoft/SEAL.git && cd SEAL && cmake . && make && make install

# 下载并安装 nlohmann/json 库
RUN git clone https://github.com/nlohmann/json.git && cd json && cmake -DBUILD_TESTING=OFF . && make install

# 复制项目
COPY . /cpp-service
WORKDIR /cpp-service

# 构建 C++ 服务
RUN cmake . && make

# 运行 C++ 服务
CMD ["./cpp-service"]
