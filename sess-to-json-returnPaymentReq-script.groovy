// sess -> JSON body script for returns, MAPRRequest1. PROPOSAL 2026-10-27, v3: aligned to the sess-to-MAPRRequest1 sheet
// (sess-to-MAPRRequest1-mapping.xlsx). Fields marked 'not available in pacs.004.001.05' on the sheet are not mapped.
// Expression (gate): ??sess.additionalData.CuscalPaymentType && ( sess.additionalData.CuscalPaymentType == 'ReturnPayment' )   // gate value still a guess

ctx.wsapi.rri = 0
ctx.wsapi.correlator = formatDate('yyyyMMddHHmmss') + format('014d',endpointSequence('Correlator_seq'))
ctx.wsapi.url='http://cuscal-out-npe.hbs.net.au:443/npp/payments/v4/HBSLAU4TXXX/payments/returns/'+ sess.paymentData.payment.PmtId.TxId   // unchanged; see note on http:// + 443
ctx.wsapi.http_method='POST'
ctx.wsapi.http_headers.'x-api-key' = "<REDACTED - keep the value from your current script>"
ctx.wsapi.http_headers.'x-auditInfo-applicationId' = "HBLNPP"
ctx.wsapi.http_headers.'x-auditInfo-endUserId' = sess.paymentData.payment.DbtrAcct.Id.Othr.Id
ctx.wsapi.http_headers.'x-traceId' = uuid()
ctx.wsapi.http_headers.'x-instruction-priority' = "NORM"   // sheet: AppHdr.Prty; see instruction_priority below

// --- payment_information ---
ext.MAPRRequest1.payment_information.creation_date_time         = sess.paymentData.AppHdr.CreDt
ext.MAPRRequest1.payment_information.transaction_identification = sess.paymentData.payment.RtrId
// Sheet: AppHdr.Prty. No sess trace shows AppHdr.Prty yet, and an unknown sess path fails even with ?:, so NORM for now.
// Once the XML->sess script has  sess.paymentData.AppHdr.Prty ?= ext.ReturnPaymentRequest.AppHdr.Prty  switch to the commented line
// (and the x-instruction-priority header to the same source).
ext.MAPRRequest1.payment_information.instruction_priority       = 'NORM'
//ext.MAPRRequest1.payment_information.instruction_priority     = sess.paymentData.AppHdr.Prty

// --- payment_return_request_information ---
// case_identification: '??' on the sheet. The XML has it as AddtlInf '/CASE/<id>'; left commented until confirmed:
// if ( ??sess.paymentData.payment.RtrRsnInf[0].AddtlInf[0] && indexOf( sess.paymentData.payment.RtrRsnInf[0].AddtlInf[0], '/CASE/' ) == 0 )
//     ext.MAPRRequest1.payment_return_request_information.case_identification ?= split( sess.paymentData.payment.RtrRsnInf[0].AddtlInf[0], '/' )[2]
ext.MAPRRequest1.payment_return_request_information.original_message_identification     = sess.paymentData.OrgnlGrpInf.OrgnlMsgId
ext.MAPRRequest1.payment_return_request_information.original_end_to_end_identification  = sess.paymentData.payment.OrgnlEndToEndId
ext.MAPRRequest1.payment_return_request_information.original_transaction_identification = sess.paymentData.payment.OrgnlTxId
ext.MAPRRequest1.payment_return_request_information.full_legal_account_name            ?= sess.paymentData.payment.Dbtr.CtctDtls.Nm
ext.MAPRRequest1.payment_return_request_information.returned_amount                     = sess.paymentData.payment.RtrdIntrBkSttlmAmt._text
ext.MAPRRequest1.payment_return_request_information.return_reason_code                  = sess.paymentData.payment.RtrRsnInf[0].Rsn.Cd
ext.MAPRRequest1.payment_return_request_information.return_additional_information      ?= sess.paymentData.payment.RtrRsnInf[0].AddtlInf   // array, copied whole

// --- sanctions_data: identification ---
// Sheet column A (the conditions) is cut off in the photos; codes below are the ones used for Make a payment.
// Unrolled on fixed indexes like the working inbound script (Othr[0..1] PrvtId, Othr[0..2] OrgId) instead of an untested for-loop.
// These sess paths are proven by the inbound sess->XML script. They stay empty until the return XML->sess script copies
// OrgnlTxRef.Cdtr.Id into sess Dbtr.Id.
if ( ??sess.paymentData.payment.Dbtr.Id.PrvtId ) {
    if ( ??sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[0].Id && sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[0].SchmeNm.Prtry == 'DBTH' )
        ext.MAPRRequest1.sanctions_data.date_of_birth ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[0].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[0].Id && sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[0].SchmeNm.Prtry == 'PBTH' )
        ext.MAPRRequest1.sanctions_data.place_of_birth ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[0].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[1].Id && sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[1].SchmeNm.Prtry == 'DBTH' )
        ext.MAPRRequest1.sanctions_data.date_of_birth ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[1].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[1].Id && sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[1].SchmeNm.Prtry == 'PBTH' )
        ext.MAPRRequest1.sanctions_data.place_of_birth ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[1].Id
} else if ( ??sess.paymentData.payment.Dbtr.Id.OrgId ) {   // sheet: incorporation/ABN only when PrvtId is null
    if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].Id && sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].SchmeNm.Prtry == 'DCRP' )
        ext.MAPRRequest1.sanctions_data.date_of_incorporation ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].Id && sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].SchmeNm.Prtry == 'PCRP' )
        ext.MAPRRequest1.sanctions_data.place_of_incorporation ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].Id && sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].Issr == 'iso6523:0151' )
        ext.MAPRRequest1.sanctions_data.business_abn ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].Id && sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].SchmeNm.Prtry == 'DCRP' )
        ext.MAPRRequest1.sanctions_data.date_of_incorporation ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].Id && sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].SchmeNm.Prtry == 'PCRP' )
        ext.MAPRRequest1.sanctions_data.place_of_incorporation ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].Id && sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].Issr == 'iso6523:0151' )
        ext.MAPRRequest1.sanctions_data.business_abn ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].Id && sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].SchmeNm.Prtry == 'DCRP' )
        ext.MAPRRequest1.sanctions_data.date_of_incorporation ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].Id && sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].SchmeNm.Prtry == 'PCRP' )
        ext.MAPRRequest1.sanctions_data.place_of_incorporation ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].Id
    if ( ??sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].Id && sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].Issr == 'iso6523:0151' )
        ext.MAPRRequest1.sanctions_data.business_abn ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].Id
}

// --- sanctions_data: address (same sess paths the inbound script reads) ---
ext.MAPRRequest1.sanctions_data.address.address_type       ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrTp
ext.MAPRRequest1.sanctions_data.address.department         ?= sess.paymentData.payment.Dbtr.PstlAdr.Dept
ext.MAPRRequest1.sanctions_data.address.sub_department     ?= sess.paymentData.payment.Dbtr.PstlAdr.SubDept
ext.MAPRRequest1.sanctions_data.address.street_name        ?= sess.paymentData.payment.Dbtr.PstlAdr.StrtNm
ext.MAPRRequest1.sanctions_data.address.bldg_number        ?= sess.paymentData.payment.Dbtr.PstlAdr.BldgNb
ext.MAPRRequest1.sanctions_data.address.postcode           ?= sess.paymentData.payment.Dbtr.PstlAdr.PstCd
ext.MAPRRequest1.sanctions_data.address.town_name          ?= sess.paymentData.payment.Dbtr.PstlAdr.TwnNm
ext.MAPRRequest1.sanctions_data.address.ctry_sub_division  ?= sess.paymentData.payment.Dbtr.PstlAdr.CtrySubDvsn
ext.MAPRRequest1.sanctions_data.address.country_code       ?= sess.paymentData.payment.Dbtr.PstlAdr.Ctry
ext.MAPRRequest1.sanctions_data.address.address_line       ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrLine   // array copied whole; if rejected use address_line[0] ?= AdrLine[0]
// Not mapped (sheet: not available in pacs.004.001.05, and not in the sess schema): bldg_name, floor, post_box, room, district_name, town_location.
// Sheet errors there: room is mapped from PstlAdr.Ctry, and town_location is missing 'address.'.

return true
