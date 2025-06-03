from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from datetime import datetime, timedelta
import jwt  # PyJWT 库
from . import models, schemas
from .database import SessionLocal
from passlib.context import CryptContext

router = APIRouter(prefix="/auth", tags=["auth"])

# 配置 JWT 密钥和算法
SECRET_KEY = "supersecretkey123"  # 实际应用应从环境变量获取
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24  # 24小时有效期

# 配置密码哈希算法
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# OAuth2 密码流，用于从请求头获取 Authorization token
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="auth/login")

def get_db():
    """FastAPI 依赖项：获取数据库会话"""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

def create_access_token(data: dict, expires_delta: timedelta = None) -> str:
    """生成 JWT token"""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    token_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return token_jwt

def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)) -> models.User:
    """依赖：从token中获取当前用户对象"""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="无法验证凭证",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")
        if user_id is None:
            raise credentials_exception
    except jwt.ExpiredSignatureError:
        # token过期
        raise HTTPException(status_code=401, detail="Token已过期，请重新登录")
    except jwt.PyJWTError:
        raise credentials_exception
    # 查询用户
    user = db.query(models.User).filter(models.User.id == int(user_id)).first()
    if user is None:
        raise credentials_exception
    return user

def get_current_teacher(current_user: models.User = Depends(get_current_user)) -> models.User:
    """依赖：确保当前用户为老师角色"""
    if current_user.role != "teacher":
        # 无权限访问教师限定接口
        raise HTTPException(status_code=403, detail="需要教师权限")
    return current_user

@router.post("/register", response_model=schemas.UserOut)
def register(user: schemas.UserCreate, db: Session = Depends(get_db)):
    # 检查用户名是否已存在
    existing = db.query(models.User).filter(models.User.username == user.username).first()
    if existing:
        raise HTTPException(status_code=400, detail="用户名已被注册")
    # 创建新用户
    new_user = models.User(username=user.username,
                            hashed_password=get_password_hash(user.password),
                            role=user.role)
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user

@router.post("/login")
def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    # OAuth2PasswordRequestForm 会以表单格式提供 username 和 password
    user = db.query(models.User).filter(models.User.username == form_data.username).first()
    if not user:
        raise HTTPException(status_code=401, detail="用户名不存在")
    if not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="密码错误")
    # 生成访问令牌
    access_token = create_access_token({"sub": str(user.id)})
    return {"access_token": access_token, "token_type": "bearer"}

@router.get("/me", response_model=schemas.UserOut)
def get_current_user_profile(current_user: models.User = Depends(get_current_user)):
    # 返回当前用户的基本信息
    return current_user