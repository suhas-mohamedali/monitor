ctx.wsapi.rri = 0
ctx.wsapi.correlator = formatDate('yyyyMMddHHmmss') + format('014d',endpointSequence('Correlator_seq'))
ctx.wsapi.url='http://cuscal-out-npe.hbs.net.au:443/npp/payments/v4/HBSLAU4TXXX/payments/returns/'+ sess.paymentData.payment.PmtId.TxId
ctx.wsapi.http_method='POST'
ctx.wsapi.http_headers.'x-api-key' = "c5e57596ac914aaab2c88647d11f5b4b"
ctx.wsapi.http_headers.'x-auditInfo-applicationId' = "HBLNPP"
ctx.wsapi.http_headers.'x-auditInfo-endUserId' = sess.paymentData.payment.DbtrAcct.Id.Othr.Id
ctx.wsapi.http_headers.'x-traceId' = uuid()
ctx.wsapi.http_headers.'x-instruction-priority' = "NORM"

// --- payment_information ---
ext.MAPRRequest1.payment_information.creation_date_time         = sess.paymentData.AppHdr.CreDt ?: sess.paymentData.GrpHdr.CreDtTm   // r4, r13
ext.MAPRRequest1.payment_information.transaction_identification = sess.paymentData.payment.PmtId.TxId ?: sess.paymentData.payment.RtrId   // r17, G2
ext.MAPRRequest1.payment_information.end_to_end_identification ?= sess.paymentData.payment.PmtId.EndToEndId ?: sess.paymentData.payment.OrgnlEndToEndId   // r16, G2
ext.MAPRRequest1.payment_information.original_message_identification ?= sess.paymentData.OrgnlGrpInf.OrgnlMsgId ?: sess.paymentData.GrpHdr.MsgId   // r10, G2
ext.MAPRRequest1.payment_information.instruction_priority      ?= sess.indicators.paymentMode   // r9
if ( ??sess.paymentData.AppHdr.BizSvc ) {   // r7, r18, G3: 'npp.clear.01-sct.01' -> 'sct.01'
  if ( indexOf( sess.paymentData.AppHdr.BizSvc, '-' ) > 0 )
     ext.MAPRRequest1.payment_information.service_level ?= split( sess.paymentData.AppHdr.BizSvc, '-' )[1]
  else
     ext.MAPRRequest1.payment_information.service_level ?= sess.paymentData.AppHdr.BizSvc
}
ext.MAPRRequest1.payment_information.instructed_amount         ?= sess.paymentData.payment.IntrBkSttlmAmt._text   // r24 (original amount)
if ( ??sess.paymentData.payment.RmtInf.Ustrd[1] )   // r19: sheet splits at 140 chars, so join both halves back
   ext.MAPRRequest1.payment_information.remittance_information_unstructured = sess.paymentData.payment.RmtInf.Ustrd[0] + sess.paymentData.payment.RmtInf.Ustrd[1]
else
   ext.MAPRRequest1.payment_information.remittance_information_unstructured ?= sess.paymentData.payment.RmtInf.Ustrd[0]
ext.MAPRRequest1.payment_information.payment_purpose_code      ?= sess.paymentData.payment.Purp.Cd   // r20
ext.MAPRRequest1.payment_information.category_purpose_code     ?= sess.paymentData.payment.PmtTpInf.CtgyPurp.Cd   // r21
ext.MAPRRequest1.payment_information.unique_superannuation_code ?= sess.paymentData.payment.UltmtCdtr.Id.OrgId.Othr[0].SchmeNm.Prtry   // r22
ext.MAPRRequest1.payment_information.unique_superannuation_identification ?= sess.paymentData.payment.UltmtCdtr.Id.OrgId.Othr[0].Id   // r23
ext.MAPRRequest1.payment_information.payroll_payment_date      ?= sess.paymentData.payment.RmtInf.Strd[0].RfrdDocInf[0].RltdDt   // r27 (inbound indexes RfrdDocInf)

ext.MAPRRequest1.transaction_status_information.acceptance_date_time ?= sess.paymentData.payment.IntrBkSttlmDt   // r28 (sess holds a date)

// --- debtor_information (sess Dbtr) ---
ext.MAPRRequest1.debtor_information.party_name                 ?= sess.paymentData.payment.Dbtr.Nm   // r29
ext.MAPRRequest1.debtor_information.ultimate_party_name        ?= sess.paymentData.payment.UltmtDbtr.Nm   // r30
ext.MAPRRequest1.debtor_information.account_servicer_bic       ?= sess.paymentData.payment.DbtrAgt.FinInstnId.BICFI ?: sess.paymentData.GrpHdr.InstgAgt.FinInstnId.BICFI   // r31, r11
ext.MAPRRequest1.debtor_information.account_identification_type_code ?= sess.paymentData.payment.DbtrAcct.Id.Othr.SchmeNm.Cd   // r32
ext.MAPRRequest1.debtor_information.account_identification     ?= sess.paymentData.payment.DbtrAcct.Id.Othr.Id   // r33
ext.MAPRRequest1.debtor_information.account_identification_issuer ?= sess.paymentData.payment.DbtrAcct.Id.Othr.Issr   // r34
// debtor_information.partyType: NA on the sheet (r38), not sent
// PayID all-three-or-none (CUS.API.100003), r35-37, G4. Sheet sess path DbtrAcct.Id.Prxy; tester showed sess.paymentData.dynamic.DbtrAcct.Prxy
if ( ??sess.paymentData.dynamic.DbtrAcct.Prxy.Tp.Prtry && ??sess.paymentData.dynamic.DbtrAcct.Prxy.Id && ??sess.paymentData.payment.Dbtr.CtctDtls.Nm ) {
    ext.MAPRRequest1.debtor_information.account_alias_type_code      ?= sess.paymentData.dynamic.DbtrAcct.Prxy.Tp.Prtry
    ext.MAPRRequest1.debtor_information.account_alias_identification ?= sess.paymentData.dynamic.DbtrAcct.Prxy.Id
    ext.MAPRRequest1.debtor_information.full_legal_account_name      ?= sess.paymentData.payment.Dbtr.CtctDtls.Nm
}


// --- creditor_information (sess Cdtr) ---
// orgId (r39, CdtrAgt BICFI) is the URL path parameter: set in the header script, not here
ext.MAPRRequest1.creditor_information.party_name               ?= sess.paymentData.payment.Cdtr.Nm   // r40
ext.MAPRRequest1.creditor_information.ultimate_party_name      ?= sess.paymentData.payment.UltmtCdtr.Nm   // r41
ext.MAPRRequest1.creditor_information.account_identification_type_code ?= sess.paymentData.payment.CdtrAcct.Id.Othr.SchmeNm.Cd   // r42
ext.MAPRRequest1.creditor_information.account_identification   ?= sess.paymentData.payment.CdtrAcct.Id.Othr.Id   // r43
//ext.returnPaymentReq.creditor_information.account_identification_issuer ?= sess.paymentData.payment.CdtrAcct.Id.Othr.Issr   // not on the sheet
ext.MAPRRequest1.creditor_information.party_reference          ?= sess.paymentData.payment.RmtInf.Strd[0].CdtrRefInf.Ref   // r47
// PayID all-three-or-none (CUS.API.100003), r44-46. stripLeft(x, 47) drops the '/' the XML carries (e.g. /TELI -> TELI)
if ( ??sess.paymentData.payment.Cdtr.CtctDtls.Othr && ??sess.paymentData.payment.Cdtr.CtctDtls.EmailAdr && ??sess.paymentData.payment.Cdtr.CtctDtls.Nm ) {
    ext.MAPRRequest1.creditor_information.account_alias_type_code      ?= stripLeft( sess.paymentData.payment.Cdtr.CtctDtls.Othr, 47 )
    ext.MAPRRequest1.creditor_information.account_alias_identification ?= sess.paymentData.payment.Cdtr.CtctDtls.EmailAdr
    ext.MAPRRequest1.creditor_information.full_legal_account_name      ?= sess.paymentData.payment.Cdtr.CtctDtls.Nm
}

// --- mandate_information ---
ext.MAPRRequest1.mandate_information.instruction_identification ?= sess.paymentData.payment.PmtId.InstrId   // r48, r69
ext.MAPRRequest1.mandate_information.initiating_party_name      ?= sess.paymentData.payment.InitgPty.Nm   // r70
ext.MAPRRequest1.mandate_information.mandate_identification     ?= sess.paymentData.payment.InitgPty.CtctDtls.Othr   // r71 (r49 has no sess source)

// --- payment_return_information ---
ext.MAPRRequest1.payment_return_information.return_reason_code  ?= sess.paymentData.payment.RtrRsnInf[0].Rsn.Cd   // r65
ext.MAPRRequest1.payment_return_information.return_additional_information ?= sess.paymentData.payment.RtrRsnInf[0].AddtlInf   // r66, array copied whole
ext.MAPRRequest1.payment_return_information.original_transaction_identification ?= sess.paymentData.payment.OrgnlTxId   // r67
ext.MAPRRequest1.payment_return_information.return_amount       ?= sess.paymentData.payment.RtrdIntrBkSttlmAmt._text   // r68

// --- sanctions_information: debtor identification, Othr[i] search (r50-54) ---
// PrvtId first; OrgId only when PrvtId is null, as the sheet says (r52-54)
if ( ??sess.paymentData.payment.Dbtr.Id.PrvtId.Othr ) {
    for ( i = 0; i < size(sess.paymentData.payment.Dbtr.Id.PrvtId.Othr); i++ ) {
        if ( sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].SchmeNm.Prtry == 'DBTH' )
            ext.MAPRRequest1.sanctions_information.date_of_birth  ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].Id
        if ( sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].SchmeNm.Prtry == 'PBTH' )
            ext.MAPRRequest1.sanctions_information.place_of_birth ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[i].Id
    }
} else if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr ) {
    for ( i = 0; i < size(sess.paymentData.payment.Dbtr.Id.OrgId.Othr); i++ ) {
        if ( sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].SchmeNm.Prtry == 'DCRP' )
            ext.MAPRRequest1.sanctions_information.date_of_incorporation  ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Id
        if ( sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].SchmeNm.Prtry == 'PCRP' )
            ext.MAPRRequest1.sanctions_information.place_of_incorporation ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Id
        if ( sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Issr == 'iso6523:0151' )
            ext.MAPRRequest1.sanctions_information.business_abn           ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[i].Id
    }
}

// --- sanctions_information: debtor postal address (r55-64, r72-77) ---
ext.MAPRRequest1.sanctions_information.address.address_type      ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrTp
ext.MAPRRequest1.sanctions_information.address.department        ?= sess.paymentData.payment.Dbtr.PstlAdr.Dept
ext.MAPRRequest1.sanctions_information.address.sub_department    ?= sess.paymentData.payment.Dbtr.PstlAdr.SubDept
ext.MAPRRequest1.sanctions_information.address.street_name       ?= sess.paymentData.payment.Dbtr.PstlAdr.StrtNm
ext.MAPRRequest1.sanctions_information.address.bldg_number       ?= sess.paymentData.payment.Dbtr.PstlAdr.BldgNb
ext.MAPRRequest1.sanctions_information.address.postcode          ?= sess.paymentData.payment.Dbtr.PstlAdr.PstCd
ext.MAPRRequest1.sanctions_information.address.town_name         ?= sess.paymentData.payment.Dbtr.PstlAdr.TwnNm
ext.MAPRRequest1.sanctions_information.address.ctry_sub_division ?= sess.paymentData.payment.Dbtr.PstlAdr.CtrySubDvsn
ext.MAPRRequest1.sanctions_information.address.country_code      ?= sess.paymentData.payment.Dbtr.PstlAdr.Ctry
ext.MAPRRequest1.sanctions_information.address.address_line      ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrLine   // array; confirm API type
// r72-77: JSON-only fields (not in pacs.004), sent if the JSON-to-sess side ever fills them
ext.MAPRRequest1.sanctions_information.address.bldg_name         ?= sess.paymentData.payment.Dbtr.PstlAdr.BldgNm
ext.MAPRRequest1.sanctions_information.address.floor             ?= sess.paymentData.payment.Dbtr.PstlAdr.Flr
ext.MAPRRequest1.sanctions_information.address.post_box          ?= sess.paymentData.payment.Dbtr.PstlAdr.PstBx
ext.MAPRRequest1.sanctions_information.address.room              ?= sess.paymentData.payment.Dbtr.PstlAdr.Room
ext.MAPRRequest1.sanctions_information.address.district_name     ?= sess.paymentData.payment.Dbtr.PstlAdr.DstrctNm
ext.MAPRRequest1.sanctions_information.address.town_location     ?= sess.paymentData.payment.Dbtr.PstlAdr.TwnLctnNm

return true
