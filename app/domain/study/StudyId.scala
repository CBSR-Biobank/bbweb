package domain.study

import domain.Identity

class StudyId(anId: String) extends { val id = anId } with Identity {
}