from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from .. import schemas, crud, database

router = APIRouter(prefix="/progress", tags=["progress"])

@router.post("/mark")
def mark_progress(progress: schemas.PPTProgressCreate, db: Session = Depends(database.get_db)):
    return crud.mark_ppt_as_viewed(db, progress)

@router.get("/user/{user_id}")
def get_progress(user_id: int, db: Session = Depends(database.get_db)):
    return crud.get_user_progress(db, user_id)
