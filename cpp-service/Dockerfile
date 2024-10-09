FROM gcc:11 as builder

# 安装 CMake
RUN apt-get update && apt-get install -y cmake

# 安装 Microsoft SEAL
RUN git clone https://github.com/microsoft/SEAL.git && cd SEAL && cmake . && make && make install

# 复制项目
COPY . /cpp-service
WORKDIR /cpp-service

# 构建 C++ 服务
RUN cmake . && make

# 运行 C++ 服务
CMD ["./cpp-service"]