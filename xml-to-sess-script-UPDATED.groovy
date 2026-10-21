// UPDATED XML-to-sess script (processCreditTransferRequest), PROPOSAL 2026-10-21.
// Changes vs current, marked // UPDATED: RgltryRptg array indexes (lines 111-112), Dbtr.Id copied into sess (after line 84).
// Lines 47-48 and 176/179 were cut off in the photos and are reconstructed; check them against the live script.

sess.messageType                          = 'FIToFICstmrCdtTrf'
sess.serviceType                          = 'CR'
sess.indicators.authorizationNotRequired = true

if (??ctx.wsapi.url_path && (size(split(stripRight(stripLeft(b, 47), 47), '/')) > 3))
    sess.additionalData.CuscalPaymentType = 'MakePayToPayment'
else
    sess.additionalData.CuscalPaymentType = 'MakePayment'

//---------------------------------------------------
// AppHdr
//---------------------------------------------------

sess.paymentData.AppHdr.Fr.FIId.FinInstnId.BICFI ?= ext.ProcessCreditTransferRequest.AppHdr.Fr.FIId.FinInstnId.BICFI
sess.paymentData.AppHdr.To.FIId.FinInstnId.BICFI ?= ext.ProcessCreditTransferRequest.AppHdr.To.FIId.FinInstnId.BICFI
sess.paymentData.AppHdr.BizMsgIdr ?= ext.ProcessCreditTransferRequest.AppHdr.BizMsgIdr
sess.paymentData.AppHdr.MsgDefIdr ?= ext.ProcessCreditTransferRequest.AppHdr.MsgDefIdr
sess.paymentData.AppHdr.BizSvc ?= ext.ProcessCreditTransferRequest.AppHdr.BizSvc
sess.paymentData.AppHdr.CreDt ?= ext.ProcessCreditTransferRequest.AppHdr.CreDt
sess.paymentData.AppHdr.PssblDplct ?= ext.ProcessCreditTransferRequest.AppHdr.PssblDplct
sess.paymentData.AppHdr.Prty ?= ext.ProcessCreditTransferRequest.AppHdr.Prty

//---------------------------------------------------
// GrpHdr
//---------------------------------------------------

sess.paymentData.GrpHdr.MsgId ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.GrpHdr.MsgId
sess.paymentData.GrpHdr.CreDtTm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.GrpHdr.CreDtTm
sess.paymentData.GrpHdr.NbOfTxs ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.GrpHdr.NbOfTxs
sess.paymentData.GrpHdr.SttlmInf.SttlmMtd ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.GrpHdr.SttlmInf.SttlmMtd
sess.paymentData.GrpHdr.SttlmInf.ClrSys.Cd ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.GrpHdr.SttlmInf.ClrSys.Cd
sess.paymentData.GrpHdr.InstgAgt.FinInstnId.BICFI ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.GrpHdr.InstgAgt.FinInstnId.BICFI
sess.paymentData.GrpHdr.InstdAgt.FinInstnId.BICFI ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.GrpHdr.InstdAgt.FinInstnId.BICFI

//---------------------------------------------------
// Payment
//---------------------------------------------------

sess.paymentData.payment.PmtId.InstrId ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.PmtId.InstrId
sess.paymentData.payment.PmtId.EndToEndId ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.PmtId.EndToEndId
sess.paymentData.payment.PmtId.TxId ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.PmtId.TxId

sess.paymentData.payment.PmtTpInf.InstrPrty ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.PmtTpInf.InstrPrty
sess.paymentData.payment.PmtTpInf.SvcLvl.Prtry ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.PmtTpInf.SvcLvl.Prtry
sess.paymentData.payment.PmtTpInf.CtgyPurp.Cd ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.PmtTpInf.CtgyPurp.Cd

sess.paymentData.payment.IntrBkSttlmAmt._Ccy ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.IntrBkSttlmAmt.getAttr('Ccy')
sess.paymentData.payment.IntrBkSttlmAmt._text ?= split(ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.IntrBkSttlmAmt, '[')[0]

sess.paymentData.payment.IntrBkSttlmDt ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.IntrBkSttlmDt

//sess.paymentData.payment.InstdAmt._Ccy ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.InstdAmt.getAttr('Ccy')
//sess.paymentData.payment.InstdAmt._text ?= split(ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.InstdAmt, '[')[0]

sess.paymentData.payment.ChrgBr ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.ChrgBr

//---------------------------------------------------
// Debtor
//---------------------------------------------------

sess.paymentData.payment.UltmtDbtr.Nm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.UltmtDbtr.Nm

sess.paymentData.payment.InitgPty.Nm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.InitgPty.Nm
sess.paymentData.payment.InitgPty.CtctDtls.Othr ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.InitgPty.CtctDtls.Othr

sess.paymentData.payment.Dbtr.Nm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.Nm
sess.paymentData.payment.Dbtr.CtctDtls.Nm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.CtctDtls.Nm

sess.paymentData.payment.Dbtr.PstlAdr.AdrTp ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.AdrTp
sess.paymentData.payment.Dbtr.PstlAdr.Dept ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.Dept
sess.paymentData.payment.Dbtr.PstlAdr.SubDept ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.SubDept
sess.paymentData.payment.Dbtr.PstlAdr.StrtNm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.StrtNm
sess.paymentData.payment.Dbtr.PstlAdr.BldgNb ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.BldgNb
//sess.paymentData.payment.Dbtr.PstlAdr.BldgNm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.
//sess.paymentData.payment.Dbtr.PstlAdr.Flr ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.Flr
//sess.paymentData.payment.Dbtr.PstlAdr.PstBx ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.PstBx
//sess.paymentData.payment.Dbtr.PstlAdr.Room ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.Room
sess.paymentData.payment.Dbtr.PstlAdr.PstCd ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.PstCd
sess.paymentData.payment.Dbtr.PstlAdr.TwnNm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.TwnNm
//sess.paymentData.payment.Dbtr.PstlAdr.DstrctNm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.DstrctNm
//sess.paymentData.payment.Dbtr.PstlAdr.TwnLctnNm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.TwnLctnNm
sess.paymentData.payment.Dbtr.PstlAdr.CtrySubDvsn ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.CtrySubDvsn
sess.paymentData.payment.Dbtr.PstlAdr.Ctry ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.Ctry
sess.paymentData.payment.Dbtr.PstlAdr.AdrLine ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.PstlAdr.AdrLine

// UPDATED: debtor identification for sanctions_information (sheet rows 63-67)
sess.paymentData.payment.Dbtr.Id ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Dbtr.Id

//---------------------------------------------------
// Debtor Account
//---------------------------------------------------

sess.paymentData.payment.DbtrAcct.Id.Othr.Id ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.DbtrAcct.Id.Othr.Id

def dbtrAcctSchemeCd = ''

if(??ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.DbtrAcct.Id.Othr.SchmeNm.Cd) {
  dbtrAcctSchemeCd = ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.DbtrAcct.Id.Othr.SchmeNm.Cd
}

def dbtrAcctSchemePrtry = ''

if(??ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.DbtrAcct.Id.Othr.SchmeNm.Prtry) {
  dbtrAcctSchemePrtry = ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.DbtrAcct.Id.Othr.SchmeNm.Prtry
}

if (??dbtrAcctSchemeCd && size(dbtrAcctSchemeCd) == 4)
    sess.paymentData.payment.DbtrAcct.Id.Othr.SchmeNm.Cd ?= dbtrAcctSchemeCd
else if (??dbtrAcctSchemePrtry && size(dbtrAcctSchemePrtry) == 4)
    sess.paymentData.payment.DbtrAcct.Id.Othr.SchmeNm.Cd ?= dbtrAcctSchemePrtry

sess.paymentData.payment.DbtrAcct.Id.Othr.Issr ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.DbtrAcct.Id.Othr.Issr

sess.paymentData.payment.RgltryRptg[0].Dtls[0].Tp ?=   // UPDATED: array indexes (EPT.0019)
    ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.RgltryRptg[0].Dtls[0].Tp

//---------------------------------------------------
// Agents
//---------------------------------------------------

sess.paymentData.payment.DbtrAgt.FinInstnId.BICFI ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.DbtrAgt.FinInstnId.BICFI
sess.paymentData.payment.CdtrAgt.FinInstnId.BICFI ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.CdtrAgt.FinInstnId.BICFI

//---------------------------------------------------
// Creditor
//---------------------------------------------------

sess.paymentData.payment.Cdtr.Nm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Cdtr.Nm
sess.paymentData.payment.Cdtr.CtctDtls.Nm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Cdtr.CtctDtls.Nm
sess.paymentData.payment.Cdtr.CtctDtls.Othr ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Cdtr.CtctDtls.Othr
sess.paymentData.payment.Cdtr.CtctDtls.EmailAdr ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Cdtr.CtctDtls.EmailAdr

//---------------------------------------------------
// Creditor Account
//---------------------------------------------------

sess.paymentData.payment.CdtrAcct.Id.Othr.Id ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.CdtrAcct.Id.Othr.Id

def cdtrAcctSchemeCd = ''

if(??ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.CdtrAcct.Id.Othr.SchmeNm.Cd) {
  cdtrAcctSchemeCd = ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.CdtrAcct.Id.Othr.SchmeNm.Cd
}
def cdtrAcctSchemePrtry = ''

if(??ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.CdtrAcct.Id.Othr.SchmeNm.Prtry) {
  cdtrAcctSchemePrtry = ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.CdtrAcct.Id.Othr.SchmeNm.Prtry
}

if (??cdtrAcctSchemeCd && size(cdtrAcctSchemeCd) == 4)
    sess.paymentData.payment.CdtrAcct.Id.Othr.SchmeNm.Cd ?= cdtrAcctSchemeCd
else if (??cdtrAcctSchemePrtry && size(cdtrAcctSchemePrtry) == 4)
    sess.paymentData.payment.CdtrAcct.Id.Othr.SchmeNm.Cd ?= cdtrAcctSchemePrtry

sess.paymentData.payment.CdtrAcct.Id.Othr.Issr ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.CdtrAcct.Id.Othr.Issr

//---------------------------------------------------
// Ultimate Creditor
//---------------------------------------------------

sess.paymentData.payment.UltmtCdtr.Nm ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.UltmtCdtr.Nm

sess.paymentData.payment.UltmtCdtr.Id.OrgId.Othr.Id ?=
    ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.UltmtCdtr.Id.OrgId.Othr.Id

sess.paymentData.payment.UltmtCdtr.Id.OrgId.Othr.SchmeNm.Prtry ?=
    ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.UltmtCdtr.Id.OrgId.Othr.SchmeNm.Prtry

//---------------------------------------------------
// Purpose
//---------------------------------------------------

sess.paymentData.payment.Purp.Cd ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.Purp.Cd

//---------------------------------------------------
// Remittance
//---------------------------------------------------

sess.paymentData.payment.RmtInf.Ustrd[0] ?=  ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.RmtInf.Ustrd[0]

if(size(ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.RmtInf.Ustrd) > 1) {
 sess.paymentData.payment.RmtInf.Ustrd[1] ?= ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.RmtInf.Ustrd[1]
}

sess.paymentData.payment.RmtInf.Strd.RfrdDocInf.RltdDt ?=
    ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.RmtInf.Strd.RfrdDocInf.RltdDt

sess.paymentData.payment.RmtInf.Strd[0].CdtrRefInf.Ref ?=
    ext.ProcessCreditTransferRequest.Document.FIToFICstmrCdtTrf.CdtTrfTxInf.RmtInf.Strd.CdtrRefInf.Ref

//---------------------------------------------------
// Context
//---------------------------------------------------

saved.wsapi.correlator = ctx.wsapi.correlator
saved.wsapi.http_headers = ctx.wsapi.http_headers
saved.wsapi.url_path = ctx.wsapi.url_path

return true
