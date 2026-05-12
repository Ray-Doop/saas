"""MinIO 图片上传工具"""
import logging
import io
from datetime import datetime
from minio import Minio
from config.settings import settings

logger = logging.getLogger(__name__)

_minio_client: Minio = None


def get_minio_client() -> Minio:
    global _minio_client
    if _minio_client is None:
        _minio_client = Minio(
            endpoint=settings.MINIO_ENDPOINT,
            access_key=settings.MINIO_ACCESS_KEY,
            secret_key=settings.MINIO_SECRET_KEY,
            secure=settings.MINIO_SECURE
        )
        # 确保 Bucket 存在
        if not _minio_client.bucket_exists(settings.MINIO_BUCKET):
            _minio_client.make_bucket(settings.MINIO_BUCKET)
            logger.info(f"创建MinIO Bucket: {settings.MINIO_BUCKET}")
    return _minio_client


async def upload_to_minio(task_code: str, filename: str, image_bytes: bytes) -> str:
    """上传图片到 MinIO, 返回访问URL"""
    client = get_minio_client()
    date_str = datetime.now().strftime("%Y%m%d")
    object_name = f"ai-gen/{date_str}/{task_code}/{filename}"

    client.put_object(
        bucket_name=settings.MINIO_BUCKET,
        object_name=object_name,
        data=io.BytesIO(image_bytes),
        length=len(image_bytes),
        content_type="image/png"
    )

    return f"http://{settings.MINIO_ENDPOINT}/{settings.MINIO_BUCKET}/{object_name}"


def get_image_info(image_bytes: bytes) -> tuple:
    """获取图片宽高和文件大小"""
    from PIL import Image
    size = len(image_bytes)
    try:
        img = Image.open(io.BytesIO(image_bytes))
        return img.width, img.height, size
    except Exception:
        return 0, 0, size
