#!/bin/bash

# 检查kubectl命令是否可用
source /etc/profile

if ! command -v kubectl &> /dev/null
then
    echo "错误: kubectl 命令未找到，请确保已安装并配置好 Kubernetes 客户端。"
    exit 1
fi

# 检查job.yaml文件是否存在
if [ ! -f "job.yaml" ]
then
    echo "错误: job.yaml 文件未找到，请确保在当前目录下。"
    exit 1
fi

# 设置默认参数
NUM_JOBS=5
BASE_NAME=$(uuidgen | tr 'A-Z' 'a-z' | tr -d '-')
 :

# 解析命令行参数
while [[ $# -gt 0 ]]
do
    case $1 in
        -n|--number)
            NUM_JOBS="$2"
            shift # 移除参数名
            shift # 移除参数值
            ;;
        -b|--base-name)
            BASE_NAME="$2"
            shift
            shift
            ;;
        -h|--help)
            echo "用法: $0 [-n|--number 数量] [-b|--base-name 基础名称] [-h|--help]"
            echo "  -n, --number: 要创建的job数量 (默认: 5)"
            echo "  -b, --base-name: job的基础名称，最终名称会是[基础名称]-[序号] (默认: pi-job)"
            echo "  -h, --help: 显示帮助信息"
            exit 0
            ;;
        *)
            echo "未知参数: $1"
            echo "使用 -h 或 --help 查看帮助信息"
            exit 1
            ;;
    esac
done

# 确保NUM_JOBS是有效的数字
if ! [[ $NUM_JOBS =~ ^[0-9]+$ ]] || [ $NUM_JOBS -lt 1 ]
then
    echo "错误: 无效的job数量，请输入大于0的整数。"
    exit 1
fi

# 循环创建多个job
for i in $(seq 1 $NUM_JOBS)
do
    # 定义job名称
    JOB_NAME="$BASE_NAME$i"
    
    echo "正在创建job: $JOB_NAME"
    
    # 使用awk命令更可靠地修改job名称，在MacOS上表现一致
    echo "正在修改job.yaml中的名称为: $JOB_NAME"
    
    # 使用awk处理文件内容，替换metadata.name字段
    awk -v new_name="$JOB_NAME" '
      BEGIN {found = 0}
      /^metadata:/ {found = 1}
      found && /^  name:/ {print "  name: " new_name; found = 0; next}
      {print}
    ' job.yaml > temp_job.yaml
    
    # 显示修改后的名称，用于调试
    echo "调试：修改后的名称为: $(grep -A 1 "metadata:" temp_job.yaml | grep "name:" | awk '{print $2}')"
    
    # 应用修改后的配置文件
    kubectl apply -f temp_job.yaml
    
    # 清理临时文件
    rm temp_job.yaml
    
    # 检查kubectl命令是否执行成功
    if [ $? -ne 0 ]
    then
        echo "警告: 创建job $JOB_NAME 时出错。"
    fi
    
    # 添加短暂延迟，避免创建太快
    sleep 1
done

# 显示创建结果
echo -e "\n已尝试创建 $NUM_JOBS 个jobs。"
echo "当前jobs列表："
kubectl get jobs --sort-by=.metadata.creationTimestamp
