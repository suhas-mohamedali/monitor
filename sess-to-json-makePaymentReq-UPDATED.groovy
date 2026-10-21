// UPDATED sess-to-JSON body script (makePaymentReq), PROPOSAL 2026-10-21.
// Expression (gate): ??sess.additionalData.CuscalPaymentType && ( sess.additionalData.CuscalPaymentType == 'MakePayment' )
// Changes vs current: PayID block, party_type array indexes, sanctions block. Everything else unchanged.
// Needs the XML-to-sess changes too: RgltryRptg[0].Dtls[0].Tp on both sides (lines 111-112) and Dbtr.Id copied into sess.


//ext.makePaymentReq.orgId                                   ?= sess.paymentData.AppHdr.To.FIId.FinInstnId.BICFI - SM
//ext.makePaymentReq.x-auditInfo-endUserSessionId             ?= ctx.alias
//ext.makePaymentReq.x-instruction-priority                   ?= sess.paymentData.AppHdr.Prty

ext.makePaymentReq.payment_information.creation_date_time      = sess.paymentData.AppHdr.CreDt ?: sess.paymentData.GrpHdr.CreDtTm
ext.makePaymentReq.payment_information.transaction_identification = sess.paymentData.payment.PmtId.TxId
ext.makePaymentReq.payment_information.instructed_amount      ?= sess.paymentData.payment.IntrBkSttlmAmt._text

//if ( ??sess.paymentData.payment.PmtTpInf.SvcLvl.Prtry )
//  if ( indexOf( sess.paymentData.payment.PmtTpInf.SvcLvl.Prtry, '-' ) > 0 )
//     ext.makePaymentReq.payment_information.service_level   ?= split( sess.paymentData.payment.PmtTpInf.SvcLvl.Prtry, '-' )[1]
//  else
//     ext.makePaymentReq.payment_information.service_level   ?= sess.paymentData.payment.PmtTpInf.SvcLvl.Prtry
//else if ( ??sess.paymentData.AppHdr.BizSvc )
//  if ( indexOf( sess.paymentData.AppHdr.BizSvc, '-' ) > 0 )
//     ext.makePaymentReq.payment_information.service_level   ?= split( sess.paymentData.AppHdr.BizSvc, '-' )[1]
//  else
//     ext.makePaymentReq.payment_information.service_level   ?= sess.paymentData.AppHdr.BizSvc
//ext.makePaymentReq.payment_information.service_level         = sess.paymentData.payment.PmtTpInf.SvcLvl.Prtry ?: sess.paymentData.AppHdr.BizSvc
ext.makePaymentReq.payment_information.service_level           = 'sct'

ext.makePaymentReq.payment_information.end_to_end_identification ?= sess.paymentData.payment.PmtId.EndToEndId
ext.makePaymentReq.payment_information.country_of_origin       ?= 'AU'

ext.makePaymentReq.payment_information.remittance_information_unstructured ?= sess.paymentData.payment.RmtInf.Ustrd[0]
ext.makePaymentReq.payment_information.payment_purpose_code    ?= sess.paymentData.payment.Purp.Cd
ext.makePaymentReq.payment_information.category_purpose_code   ?= sess.paymentData.payment.PmtTpInf.CtgyPurp.Cd
//ext.makePaymentReq.payment_information.payroll_payment_date
ext.makePaymentReq.payment_information.unique_superannuation_code ?= sess.paymentData.payment.UltmtCdtr.Id.OrgId.Othr.SchmeNm.Prtry
ext.makePaymentReq.payment_information.unique_superannuation_identification ?= sess.paymentData.payment.UltmtCdtr.Id.OrgId.Othr.Id
//ext.makePaymentReq.payment_information.first_payment
//ext.makePaymentReq.payment_information.cop_request_identification
//ext.makePaymentReq.payment_information.cop_result_code
ext.makePaymentReq.payment_information.instruction_priority    ?= sess.indicators.paymentMode
//ext.makePaymentReq.payment_information.original_message_identification ?= sess.paymentData.GrpHdr.MsgId


//ext.makePaymentReq.debtor_information.account_servicer_bic    = sess.paymentData.payment.DbtrAgt.FinInstnId.BICFI ?: sess.pay
ext.makePaymentReq.debtor_information.party_name               ?= sess.paymentData.payment.Dbtr.Nm
ext.makePaymentReq.debtor_information.account_identification_type_code ?= sess.paymentData.payment.DbtrAcct.Id.Othr.SchmeNm.Cd
ext.makePaymentReq.debtor_information.account_identification   ?= sess.paymentData.payment.DbtrAcct.Id.Othr.Id
ext.makePaymentReq.debtor_information.account_identification_issuer ?= sess.paymentData.payment.DbtrAcct.Id.Othr.Issr
ext.makePaymentReq.debtor_information.full_legal_account_name  ?= sess.paymentData.payment.Dbtr.CtctDtls.Nm
ext.makePaymentReq.debtor_information.party_type                = sess.paymentData.payment.RgltryRptg[0].Dtls[0].Tp ?: 'INDV'   // UPDATED: array indexes (EPT.0019)
ext.makePaymentReq.debtor_information.ultimate_party_name      ?= sess.paymentData.payment.UltmtDbtr.Nm

ext.makePaymentReq.creditor_information.party_name             ?= sess.paymentData.payment.Cdtr.Nm
ext.makePaymentReq.creditor_information.account_identification_type_code ?= sess.paymentData.payment.CdtrAcct.Id.Othr.SchmeNm.Cd
ext.makePaymentReq.creditor_information.account_identification ?= sess.paymentData.payment.CdtrAcct.Id.Othr.Id
ext.makePaymentReq.creditor_information.ultimate_party_name    ?= sess.paymentData.payment.UltmtCdtr.Nm

// UPDATED: PayID fields sent all-three-or-none (CUS.API.100003)
if ( ??sess.paymentData.payment.Cdtr.CtctDtls.Othr && ??sess.paymentData.payment.Cdtr.CtctDtls.EmailAdr && ??sess.paymentData.payment.Cdtr.CtctDtls.Nm ) {
    ext.makePaymentReq.creditor_information.account_alias_type_code      ?= sess.paymentData.payment.Cdtr.CtctDtls.Othr
    ext.makePaymentReq.creditor_information.account_alias_identification ?= sess.paymentData.payment.Cdtr.CtctDtls.EmailAdr
    ext.makePaymentReq.creditor_information.full_legal_account_name      ?= sess.paymentData.payment.Cdtr.CtctDtls.Nm
}
//ext.makePaymentReq.creditor_information.party_reference      ?= sess.paymentData.payment.RmtInf.Strd[0].CdtrRefInf.Ref

// UPDATED: sanctions_information, aligned to sheet rows 63-72, 77-78, 81-83
// --- Debtor identification: search Othr[] by scheme (sheet rows 63-67; full column B confirmed 2026-10-21) ---
// Sheet row 66 (PCRP) reads Dbtr.Id.PrvtId.Othr[i].Id after testing OrgId.Othr[i]: treated as a sheet error, OrgId used below.
if ( ??sess.paymentData.payment.Dbtr.Id.PrvtId.Othr ) {
    for ( i = 0; i < size(sess.paymentData.payment.Dbtr.Id.PrvtId.Othr); i++ ) {
        if ( sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].SchmeNm.Prtry == 'DBTH' )
            ext.makePaymentReq.sanctions_information.date_of_birth  ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].Id
        if ( sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].SchmeNm.Prtry == 'PBTH' )
            ext.makePaymentReq.sanctions_information.place_of_birth ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].Id
    }
}
if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr ) {
    for ( i = 0; i < size(sess.paymentData.payment.Dbtr.Id.OrgId.Othr); i++ ) {
        if ( sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].SchmeNm.Prtry == 'DCRP' )
            ext.makePaymentReq.sanctions_information.date_of_incorporation  ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Id
        if ( sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].SchmeNm.Prtry == 'PCRP' )
            ext.makePaymentReq.sanctions_information.place_of_incorporation ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Id
        if ( sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Issr == 'iso6523:0151' )
            ext.makePaymentReq.sanctions_information.business_abn           ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Id
    }
}

// --- Debtor postal address (sheet rows 68-72, 77-78, 81-83) ---
ext.makePaymentReq.sanctions_information.address.address_type      ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrTp
ext.makePaymentReq.sanctions_information.address.department        ?= sess.paymentData.payment.Dbtr.PstlAdr.Dept
ext.makePaymentReq.sanctions_information.address.sub_department    ?= sess.paymentData.payment.Dbtr.PstlAdr.SubDept
ext.makePaymentReq.sanctions_information.address.street_name       ?= sess.paymentData.payment.Dbtr.PstlAdr.StrtNm
ext.makePaymentReq.sanctions_information.address.bldg_number       ?= sess.paymentData.payment.Dbtr.PstlAdr.BldgNb
ext.makePaymentReq.sanctions_information.address.postcode          ?= sess.paymentData.payment.Dbtr.PstlAdr.PstCd
ext.makePaymentReq.sanctions_information.address.town_name         ?= sess.paymentData.payment.Dbtr.PstlAdr.TwnNm
ext.makePaymentReq.sanctions_information.address.ctry_sub_division ?= sess.paymentData.payment.Dbtr.PstlAdr.CtrySubDvsn
ext.makePaymentReq.sanctions_information.address.country_code      ?= sess.paymentData.payment.Dbtr.PstlAdr.Ctry
ext.makePaymentReq.sanctions_information.address.address_line      ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrLine   // array; confirm API type

return true
