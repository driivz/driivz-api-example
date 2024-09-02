package com.driivz.example.stripe

import com.stripe.android.model.CardParams

const val PARAM_NUMBER = "number"
const val PARAM_EXP_MONTH = "exp_month"
const val PARAM_EXP_YEAR = "exp_year"
const val PARAM_CVC = "cvc"
const val PARAM_NAME = "name"

fun CardParams.name() = this.typeDataParams[PARAM_NAME] as String
fun CardParams.number() = this.typeDataParams[PARAM_NUMBER] as String
fun CardParams.expMonth() = this.typeDataParams[PARAM_EXP_MONTH] as Int
fun CardParams.expYear() = this.typeDataParams[PARAM_EXP_YEAR] as Int
fun CardParams.cvc() = this.typeDataParams[PARAM_CVC] as String