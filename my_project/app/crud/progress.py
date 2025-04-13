from sqlalchemy.orm import Session
from . import models, schemas

def mark_ppt_as_viewed(db: Session, progress: schemas.PPTProgressCreate):
    db_progress = db.query(models.PPTProgress).filter_by(
        user_id=progress.user_id,
        ppt_id=progress.ppt_id
    ).first()

    if db_progress:
        db_progress.is_viewed = progress.is_viewed
    else:
        db_progress = models.PPTProgress(**progress.dict())
        db.add(db_progress)

    db.commit()
    db.refresh(db_progress)
    return db_progress

def get_user_progress(db: Session, user_id: int):
    return db.query(models.PPTProgress).filter_by(user_id=user_id).all()
