// sess -> JSON body script for returns. PROPOSAL 2026-10-26, not deployed.
// Built from the Receive Payment Return sheet (JSON col A <- sess col B, row numbers in [rN]) with the syntax of the
// Make a payment body script (../sess-to-json-makePaymentReq-UPDATED.groovy).
// No party swap here: the swap happens between sess and XML. sess Dbtr already holds the JSON debtor (the return's debtor,
// i.e. the original creditor), so debtor_information <- sess Dbtr and creditor_information <- sess Cdtr.
// GUESSES (confirm before deploying):
//   G1 ext root 'returnPaymentReq' and the gate value 'ReturnPayment'; use the names from the Return Payment API spec.
//   G2 sess fallbacks (?:) to the fields the inbound return XML->sess script actually writes (OrgnlEndToEndId, OrgnlGrpInf, RtrId).
//   G3 service_level derived by stripping the BizSvc prefix (sheet r7). Make a payment hardcodes 'sct'; use that if the API wants it.
//   G4 PayID all-three-or-none applied to the debtor as well as the creditor.
// Expression (gate): ??sess.additionalData.CuscalPaymentType && ( sess.additionalData.CuscalPaymentType == 'ReturnPayment' )   // G1

// --- payment_information ---
ext.returnPaymentReq.payment_information.creation_date_time         = sess.paymentData.AppHdr.CreDt ?: sess.paymentData.GrpHdr.CreDtTm   // r4, r13
ext.returnPaymentReq.payment_information.transaction_identification = sess.paymentData.payment.PmtId.TxId ?: sess.paymentData.payment.RtrId   // r17, G2
ext.returnPaymentReq.payment_information.end_to_end_identification ?= sess.paymentData.payment.PmtId.EndToEndId ?: sess.paymentData.payment.OrgnlEndToEndId   // r16, G2
ext.returnPaymentReq.payment_information.original_message_identification ?= sess.paymentData.OrgnlGrpInf.OrgnlMsgId ?: sess.paymentData.GrpHdr.MsgId   // r10, G2
ext.returnPaymentReq.payment_information.instruction_priority      ?= sess.indicators.paymentMode   // r9
if ( ??sess.paymentData.AppHdr.BizSvc ) {   // r7, r18, G3: 'npp.clear.01-sct.01' -> 'sct.01'
  if ( indexOf( sess.paymentData.AppHdr.BizSvc, '-' ) > 0 )
     ext.returnPaymentReq.payment_information.service_level ?= split( sess.paymentData.AppHdr.BizSvc, '-' )[1]
  else
     ext.returnPaymentReq.payment_information.service_level ?= sess.paymentData.AppHdr.BizSvc
}
ext.returnPaymentReq.payment_information.instructed_amount         ?= sess.paymentData.payment.IntrBkSttlmAmt._text   // r24 (original amount)
if ( ??sess.paymentData.payment.RmtInf.Ustrd[1] )   // r19: sheet splits at 140 chars, so join both halves back
   ext.returnPaymentReq.payment_information.remittance_information_unstructured = sess.paymentData.payment.RmtInf.Ustrd[0] + sess.paymentData.payment.RmtInf.Ustrd[1]
else
   ext.returnPaymentReq.payment_information.remittance_information_unstructured ?= sess.paymentData.payment.RmtInf.Ustrd[0]
ext.returnPaymentReq.payment_information.payment_purpose_code      ?= sess.paymentData.payment.Purp.Cd   // r20
ext.returnPaymentReq.payment_information.category_purpose_code     ?= sess.paymentData.payment.PmtTpInf.CtgyPurp.Cd   // r21
ext.returnPaymentReq.payment_information.unique_superannuation_code ?= sess.paymentData.payment.UltmtCdtr.Id.OrgId.Othr[0].SchmeNm.Prtry   // r22
ext.returnPaymentReq.payment_information.unique_superannuation_identification ?= sess.paymentData.payment.UltmtCdtr.Id.OrgId.Othr[0].Id   // r23
ext.returnPaymentReq.payment_information.payroll_payment_date      ?= sess.paymentData.payment.RmtInf.Strd[0].RfrdDocInf[0].RltdDt   // r27 (inbound indexes RfrdDocInf)

ext.returnPaymentReq.transaction_status_information.acceptance_date_time ?= sess.paymentData.payment.IntrBkSttlmDt   // r28 (sess holds a date)

// --- debtor_information (sess Dbtr) ---
ext.returnPaymentReq.debtor_information.party_name                 ?= sess.paymentData.payment.Dbtr.Nm   // r29
ext.returnPaymentReq.debtor_information.ultimate_party_name        ?= sess.paymentData.payment.UltmtDbtr.Nm   // r30
ext.returnPaymentReq.debtor_information.account_servicer_bic       ?= sess.paymentData.payment.DbtrAgt.FinInstnId.BICFI ?: sess.paymentData.GrpHdr.InstgAgt.FinInstnId.BICFI   // r31, r11
ext.returnPaymentReq.debtor_information.account_identification_type_code ?= sess.paymentData.payment.DbtrAcct.Id.Othr.SchmeNm.Cd   // r32
ext.returnPaymentReq.debtor_information.account_identification     ?= sess.paymentData.payment.DbtrAcct.Id.Othr.Id   // r33
ext.returnPaymentReq.debtor_information.account_identification_issuer ?= sess.paymentData.payment.DbtrAcct.Id.Othr.Issr   // r34
// debtor_information.partyType: NA on the sheet (r38), not sent
// PayID all-three-or-none (CUS.API.100003), r35-37, G4. Sheet sess path DbtrAcct.Id.Prxy; tester showed sess.paymentData.dynamic.DbtrAcct.Prxy
if ( ( ??sess.paymentData.payment.DbtrAcct.Id.Prxy.Tp.Prtry || ??sess.paymentData.dynamic.DbtrAcct.Prxy.Tp.Prtry ) && ( ??sess.paymentData.payment.DbtrAcct.Id.Prxy.Id || ??sess.paymentData.dynamic.DbtrAcct.Prxy.Id ) && ??sess.paymentData.payment.Dbtr.CtctDtls.Nm ) {
    ext.returnPaymentReq.debtor_information.account_alias_type_code      ?= sess.paymentData.payment.DbtrAcct.Id.Prxy.Tp.Prtry ?: sess.paymentData.dynamic.DbtrAcct.Prxy.Tp.Prtry
    ext.returnPaymentReq.debtor_information.account_alias_identification ?= sess.paymentData.payment.DbtrAcct.Id.Prxy.Id ?: sess.paymentData.dynamic.DbtrAcct.Prxy.Id
    ext.returnPaymentReq.debtor_information.full_legal_account_name      ?= sess.paymentData.payment.Dbtr.CtctDtls.Nm
}

// --- creditor_information (sess Cdtr) ---
// orgId (r39, CdtrAgt BICFI) is the URL path parameter: set in the header script, not here
ext.returnPaymentReq.creditor_information.party_name               ?= sess.paymentData.payment.Cdtr.Nm   // r40
ext.returnPaymentReq.creditor_information.ultimate_party_name      ?= sess.paymentData.payment.UltmtCdtr.Nm   // r41
ext.returnPaymentReq.creditor_information.account_identification_type_code ?= sess.paymentData.payment.CdtrAcct.Id.Othr.SchmeNm.Cd   // r42
ext.returnPaymentReq.creditor_information.account_identification   ?= sess.paymentData.payment.CdtrAcct.Id.Othr.Id   // r43
//ext.returnPaymentReq.creditor_information.account_identification_issuer ?= sess.paymentData.payment.CdtrAcct.Id.Othr.Issr   // not on the sheet
ext.returnPaymentReq.creditor_information.party_reference          ?= sess.paymentData.payment.RmtInf.Strd[0].CdtrRefInf.Ref   // r47
// PayID all-three-or-none (CUS.API.100003), r44-46. stripLeft(x, 47) drops the '/' the XML carries (e.g. /TELI -> TELI)
if ( ??sess.paymentData.payment.Cdtr.CtctDtls.Othr && ??sess.paymentData.payment.Cdtr.CtctDtls.EmailAdr && ??sess.paymentData.payment.Cdtr.CtctDtls.Nm ) {
    ext.returnPaymentReq.creditor_information.account_alias_type_code      ?= stripLeft( sess.paymentData.payment.Cdtr.CtctDtls.Othr, 47 )
    ext.returnPaymentReq.creditor_information.account_alias_identification ?= sess.paymentData.payment.Cdtr.CtctDtls.EmailAdr
    ext.returnPaymentReq.creditor_information.full_legal_account_name      ?= sess.paymentData.payment.Cdtr.CtctDtls.Nm
}

// --- mandate_information ---
ext.returnPaymentReq.mandate_information.instruction_identification ?= sess.paymentData.payment.PmtId.InstrId   // r48, r69
ext.returnPaymentReq.mandate_information.initiating_party_name      ?= sess.paymentData.payment.InitgPty.Nm   // r70
ext.returnPaymentReq.mandate_information.mandate_identification     ?= sess.paymentData.payment.InitgPty.CtctDtls.Othr   // r71 (r49 has no sess source)

// --- payment_return_information ---
ext.returnPaymentReq.payment_return_information.return_reason_code  ?= sess.paymentData.payment.RtrRsnInf[0].Rsn.Cd   // r65
ext.returnPaymentReq.payment_return_information.return_additional_information ?= sess.paymentData.payment.RtrRsnInf[0].AddtlInf   // r66, array copied whole
ext.returnPaymentReq.payment_return_information.original_transaction_identification ?= sess.paymentData.payment.OrgnlTxId   // r67
ext.returnPaymentReq.payment_return_information.return_amount       ?= sess.paymentData.payment.RtrdIntrBkSttlmAmt._text   // r68

// --- sanctions_information: debtor identification, Othr[i] search (r50-54) ---
// PrvtId first; OrgId only when PrvtId is null, as the sheet says (r52-54)
if ( ??sess.paymentData.payment.Dbtr.Id.PrvtId.Othr ) {
    for ( i = 0; i < size(sess.paymentData.payment.Dbtr.Id.PrvtId.Othr); i++ ) {
        if ( sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].SchmeNm.Prtry == 'DBTH' )
            ext.returnPaymentReq.sanctions_information.date_of_birth  ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].Id
        if ( sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].SchmeNm.Prtry == 'PBTH' )
            ext.returnPaymentReq.sanctions_information.place_of_birth ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].Id
    }
} else if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr ) {
    for ( i = 0; i < size(sess.paymentData.payment.Dbtr.Id.OrgId.Othr); i++ ) {
        if ( sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].SchmeNm.Prtry == 'DCRP' )
            ext.returnPaymentReq.sanctions_information.date_of_incorporation  ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Id
        if ( sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].SchmeNm.Prtry == 'PCRP' )
            ext.returnPaymentReq.sanctions_information.place_of_incorporation ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Id
        if ( sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Issr == 'iso6523:0151' )
            ext.returnPaymentReq.sanctions_information.business_abn           ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Id
    }
}

// --- sanctions_information: debtor postal address (r55-64, r72-77) ---
ext.returnPaymentReq.sanctions_information.address.address_type      ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrTp
ext.returnPaymentReq.sanctions_information.address.department        ?= sess.paymentData.payment.Dbtr.PstlAdr.Dept
ext.returnPaymentReq.sanctions_information.address.sub_department    ?= sess.paymentData.payment.Dbtr.PstlAdr.SubDept
ext.returnPaymentReq.sanctions_information.address.street_name       ?= sess.paymentData.payment.Dbtr.PstlAdr.StrtNm
ext.returnPaymentReq.sanctions_information.address.bldg_number       ?= sess.paymentData.payment.Dbtr.PstlAdr.BldgNb
ext.returnPaymentReq.sanctions_information.address.postcode          ?= sess.paymentData.payment.Dbtr.PstlAdr.PstCd
ext.returnPaymentReq.sanctions_information.address.town_name         ?= sess.paymentData.payment.Dbtr.PstlAdr.TwnNm
ext.returnPaymentReq.sanctions_information.address.ctry_sub_division ?= sess.paymentData.payment.Dbtr.PstlAdr.CtrySubDvsn
ext.returnPaymentReq.sanctions_information.address.country_code      ?= sess.paymentData.payment.Dbtr.PstlAdr.Ctry
ext.returnPaymentReq.sanctions_information.address.address_line      ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrLine   // array; confirm API type
// r72-77: JSON-only fields (not in pacs.004), sent if the JSON-to-sess side ever fills them
ext.returnPaymentReq.sanctions_information.address.bldg_name         ?= sess.paymentData.payment.Dbtr.PstlAdr.BldgNm
ext.returnPaymentReq.sanctions_information.address.floor             ?= sess.paymentData.payment.Dbtr.PstlAdr.Flr
ext.returnPaymentReq.sanctions_information.address.post_box          ?= sess.paymentData.payment.Dbtr.PstlAdr.PstBx
ext.returnPaymentReq.sanctions_information.address.room              ?= sess.paymentData.payment.Dbtr.PstlAdr.Room
ext.returnPaymentReq.sanctions_information.address.district_name     ?= sess.paymentData.payment.Dbtr.PstlAdr.DstrctNm
ext.returnPaymentReq.sanctions_information.address.town_location     ?= sess.paymentData.payment.Dbtr.PstlAdr.TwnLctnNm

return true
