package com.rlrio.model

import com.rlrio.annotations.Column

data class Person(
    @Column("Name")
    val name: String?,
    @Column
    val age: Int?,
    @Column("Email")
    val email: String?
)
