package com.moddakir.moddakir.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var currentlyLogged: Boolean = false,
    val id: String,
    var accessToken: String,
    val teacherPreferenceLabel: String,
    val totalPreferanceScore: Double,
    val fullName: String,
    val email: String?,
    var lastTime: Long = 0,
    var isChildDependent: Boolean = false,
    var orderForChild: Int = 0,
    val phone: String,
    val gender: String,
    val username: String,
    val pass: String,
    val country: String,
    val city: String,
    val brief: String,
    val certificate: String,
    val address: String,
    val status: String,
    val avatarUrl: String,
    val logged_in: Boolean = false,
    val isFav: Boolean = false,
    val isSubscribed: Boolean = false,
    val ejaza: String,
    val languages: String,
    val yearsOfExperience: String,
    val totalStudents: String,
    val availableTimePerWeek: String,
    val sinchId: String,
    val technicalExperience: String,
    val parent: String,
    val requested_date: String,
    val search_type: String,
    val rate: Float = 0f,
    val totalRate: Float = 0f,
    val isDependentManager: Boolean = false,
    val enableVoiceRecording: Boolean = false,
    val enableVideoRecording: Boolean = false,
    val assignmentValue: Float = 0f,
    val studentComments: List<TeacherCategoryModel>,
    val videoUrl: String?,
    val nearestSlotTime: String?,
    val currency: String?,
    val isMailActivated: Boolean = false,
    val isMobileActivated: Boolean = false,
    val education: Education?,
    val childAgePreferanceForEdit: String = "",
    val childPathId: String = "",
    val childEducationPlanId: String = "",
    val childLinesPerSession: Int = 0,


    ) : Parcelable