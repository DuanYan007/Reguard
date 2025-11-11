# GPU错误

GPU处理失败，尝试使用CPU处理

错误详情: is_bfloat16_supported(): incompatible function arguments. The following argument types are supported:
    1. (arg0: paddle.base.libpaddle.CUDAPlace) -> bool
    2. (arg0: paddle.base.libpaddle.CPUPlace) -> bool

Invoked with: Place(undefined:0)

建议：
1. 检查CUDA驱动是否正确安装
2. 或者将device参数改为"cpu"
