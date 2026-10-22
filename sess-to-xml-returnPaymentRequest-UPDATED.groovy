// UPDATED (proposal, 2026-10-22, not deployed). Original lines 1-61 kept in place; changed lines end with //was or are listed below.
// Lines 13-14 (InstdAgt CUSCAL_BIC, CreDtTm from AppHdr) left as is: inbound does the same, waiting on OCP.
// Sanctions Id and address written to OrgnlTxRef.Cdtr, waiting on the Cdtr/Dbtr decision.
ctx.wsapi.rri = 0
ctx.wsapi.correlator = formatDate('yyyyMMddHHmmss') + format('014d',endpointSequence('Correlator_seq'))
ctx.message_type='ReturnPaymentRequest'
ext.ReturnPaymentRequest.AppHdr.CreDt ?= sess.paymentData.AppHdr.CreDt
ext.ReturnPaymentRequest.AppHdr.Prty ?= sess.indicators.paymentMode //was = 'NORM'
ext.ReturnPaymentRequest.AppHdr.To.FIId.FinInstnId.BICFI ?= sess.paymentData.AppHdr.To.FIId.FinInstnId.BICFI
ext.ReturnPaymentRequest.AppHdr.Fr.FIId.FinInstnId.BICFI ?= distributedGet( 'CUSCAL_BIC' ) //sess.paymentData.AppHdr.Fr.FIId.FinInstnId.BICFI
ext.ReturnPaymentRequest.AppHdr.BizMsgIdr ?= sess.paymentData.AppHdr.BizMsgIdr
ext.ReturnPaymentRequest.AppHdr.BizSvc ?= sess.paymentData.AppHdr.BizSvc
ext.ReturnPaymentRequest.AppHdr.MsgDefIdr = 'pacs.004.001.05' //was ?= sess.paymentData.AppHdr.MsgDefIdr
ext.ReturnPaymentRequest.Document.PmtRtr.GrpHdr.MsgId ?= sess.paymentData.AppHdr.BizMsgIdr //sess.paymentData.GrpHdr.MsgId
ext.ReturnPaymentRequest.Document.PmtRtr.GrpHdr.InstgAgt.FinInstnId.BICFI ?= sess.paymentData.GrpHdr.InstgAgt.FinInstnId.BICFI
ext.ReturnPaymentRequest.Document.PmtRtr.GrpHdr.InstdAgt.FinInstnId.BICFI ?= distributedGet( 'CUSCAL_BIC' )//sess.paymentData.GrpHdr.InstdAgt.FinInstnId.BICFI
ext.ReturnPaymentRequest.Document.PmtRtr.GrpHdr.CreDtTm ?= sess.paymentData.AppHdr.CreDt //sess.paymentData.GrpHdr.CreDtTm;
ext.ReturnPaymentRequest.Document.PmtRtr.GrpHdr.NbOfTxs ?= sess.paymentData.GrpHdr.NbOfTxs;
ext.ReturnPaymentRequest.Document.PmtRtr.GrpHdr.SttlmInf.SttlmMtd ?= sess.paymentData.GrpHdr.SttlmInf.SttlmMtd;

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlEndToEndId ?= sess.paymentData.payment.PmtId.EndToEndId;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.RtrId ?= sess.paymentData.payment.PmtId.TxId;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.PmtTpInf.SvcLvl.Prtry ?= sess.paymentData.payment.PmtTpInf.SvcLvl.Prtry;

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Purp.Cd ?= sess.paymentData.payment.Purp.Cd;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.PmtTpInf.CtgyPurp.Cd ?= sess.paymentData.payment.PmtTpInf.CtgyPurp.Cd;

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.UltmtCdtr.Id.OrgId.Othr[0].SchmeNm.Prtry ?= sess.paymentData.payment.UltmtCdtr.Id.OrgId.Othr[0].SchmeNm.Prtry;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.UltmtCdtr.Id.OrgId.Othr[0].Id ?= sess.paymentData.payment.UltmtCdtr.Id.OrgId.Othr[0].Id;

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlIntrBkSttlmAmt ?= sess.paymentData.payment.IntrBkSttlmAmt._text;
//ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlIntrBkSttlmAmt.Ccy = sess.paymentData.payment.IntrBkSttlmAmt._Ccy;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlIntrBkSttlmAmt.setAttr('Ccy', sess.paymentData.payment.IntrBkSttlmAmt._Ccy) // SM fix

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Nm ?= sess.paymentData.payment.Dbtr.Nm;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.UltmtCdtr.Nm ?= sess.paymentData.payment.UltmtDbtr.Nm;

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.CdtrAgt.FinInstnId.BICFI ?= sess.paymentData.payment.DbtrAgt.FinInstnId.BICFI;

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.CdtrAcct.Id.Othr.SchmeNm.Cd ?= sess.paymentData.payment.DbtrAcct.Id.Othr.SchmeNm.Cd;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.CdtrAcct.Id.Othr.Id ?= sess.paymentData.payment.DbtrAcct.Id.Othr.Id;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.CdtrAcct.Id.Othr.Issr ?= sess.paymentData.payment.DbtrAcct.Id.Othr.Issr;

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.CtctDtls.Nm ?= sess.paymentData.payment.Dbtr.CtctDtls.Nm;

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.DbtrAgt.FinInstnId.BICFI ?= sess.paymentData.payment.CdtrAgt.FinInstnId.BICFI;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Dbtr.Nm ?= sess.paymentData.payment.Cdtr.Nm;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.UltmtDbtr.Nm ?= sess.paymentData.payment.UltmtCdtr.Nm; //was OrgnlTxRef.UltmtCdtr.Nm

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.DbtrAcct.Id.Othr.SchmeNm.Cd ?= sess.paymentData.payment.CdtrAcct.Id.Othr.SchmeNm.Cd;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.DbtrAcct.Id.Othr.Id ?= sess.paymentData.payment.CdtrAcct.Id.Othr.Id;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.DbtrAcct.Id.Othr.Issr ?= sess.paymentData.payment.CdtrAcct.Id.Othr.Issr

ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Dbtr.CtctDtls.Nm ?= sess.paymentData.payment.Cdtr.CtctDtls.Nm;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.RmtInf.Strd[0].CdtrRefInf.Ref ?= sess.paymentData.payment.RmtInf.Strd[0].CdtrRefInf.Ref;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxId ?= sess.paymentData.payment.OrgnlTxId;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.RtrdIntrBkSttlmAmt ?= sess.paymentData.payment.RtrdIntrBkSttlmAmt._text;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.RtrdIntrBkSttlmAmt.setAttr('Ccy', sess.paymentData.payment.RtrdIntrBkSttlmAmt._Ccy) //SM fix
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlInstrId ?= sess.paymentData.payment.PmtId.InstrId;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.RtrRsnInf.Rsn.Cd ?= sess.paymentData.payment.RtrRsnInf[0].Rsn.Cd;
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.RtrRsnInf.AddtlInf ?= sess.paymentData.payment.RtrRsnInf[0].AddtlInf;
ext.ReturnPaymentRequest.Document.PmtRtr.OrgnlGrpInf.OrgnlCreDtTm ?= sess.paymentData.AppHdr.CreDt // SM fix
ext.ReturnPaymentRequest.Document.PmtRtr.OrgnlGrpInf.OrgnlMsgId ?= sess.paymentData.GrpHdr.MsgId // SM fix
ext.ReturnPaymentRequest.Document.PmtRtr.OrgnlGrpInf.OrgnlMsgNmId = 'pacs.008.001.05' //SM fix

// ===== Added from sess-to-xml-fix-proposal.groovy (syntax from the working inbound script) =====
// --- New (defect 4): inbound 18-20 pattern. Inbound sends Ustrd[0] only; the second block covers the sheet's 140-char split
// ext Ustrd is not an array (inbound 19 assigns it without an index), so join the two sess halves back into one value
if (??sess.paymentData.payment.RmtInf.Ustrd[1]) {
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.RmtInf.Ustrd = sess.paymentData.payment.RmtInf.Ustrd[0] + sess.paymentData.payment.RmtInf.Ustrd[1]
} else if (??sess.paymentData.payment.RmtInf) {
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.RmtInf.Ustrd = sess.paymentData.payment.RmtInf.Ustrd[0]
}

// --- New: sheet rows 26-27, inbound 27, 30, 144
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.ChrgBr = 'SLEV'
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.RmtInf.Strd[0].RfrdDocInf[0].RltdDt ?= sess.paymentData.payment.RmtInf.Strd[0].RfrdDocInf[0].RltdDt
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.RmtInf.Strd[0].RfrdDocInf[0].Nb ?= sess.paymentData.payment.RmtInf.Strd[0].RfrdDocInf[0].Nb

// --- New (defect 27): inbound 32-37, copied as is
def isoFormat = "yyyy-MM-dd"
def inputDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
def IntrBkSttlmDtFormattedDate = formatDate(isoFormat, sess.paymentData.payment.IntrBkSttlmDt , inputDateFormat)
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.IntrBkSttlmDt ?= IntrBkSttlmDtFormattedDate

// --- New (defects 11-12, 13): creditor PayID via CtctDtls, inbound 65, 69, 71. pacs.004 has no Prxy, but OrgnlTxRef.Dbtr
// has CtctDtls like pacs.008 does. Only works once JSON-to-sess fills sess Cdtr.CtctDtls (tester: input never reaches sess).
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Dbtr.CtctDtls.Othr ?= "/" + sess.paymentData.payment.Cdtr.CtctDtls.Othr
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Dbtr.CtctDtls.EmailAdr ?= sess.paymentData.payment.Cdtr.CtctDtls.EmailAdr
// (line 51 already maps Cdtr.CtctDtls.Nm)
// Debtor PayID (defects 6-7): inbound has no debtor alias lines either. Leave unmapped unless OCP asks for Cdtr.CtctDtls.

// --- Block C (defects 15-16, plus DCRP/PCRP/ABN): inbound 75-99, same positional indexes
if (??sess.paymentData.payment.Dbtr.Id.PrvtId) {
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Id.PrvtId.Othr[0].SchmeNm.Prtry ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[0].SchmeNm.Prtry
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Id.PrvtId.Othr[0].Id ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[0].Id
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Id.PrvtId.Othr[1].SchmeNm.Prtry ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[1].SchmeNm.Prtry
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Id.PrvtId.Othr[1].Id ?= sess.paymentData.payment.Dbtr.Id.PrvtId.Othr[1].Id
} else if (??sess.paymentData.payment.Dbtr.Id.OrgId)  {
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Id.OrgId.Othr[0].SchmeNm.Prtry ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].SchmeNm.Prtry
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Id.OrgId.Othr[0].Id ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[0].Id
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Id.OrgId.Othr[1].SchmeNm.Prtry ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].SchmeNm.Prtry
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Id.OrgId.Othr[1].Id ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[1].Id
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Id.OrgId.Othr[2].Issr ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].Issr
  ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.Id.OrgId.Othr[2].Id ?= sess.paymentData.payment.Dbtr.Id.OrgId.Othr[2].Id
}

// --- Block D (defects 17-26): inbound 105-123. Inbound sends AdrLine[0] only; [1] added because the sheet row is an array
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.AdrTp ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrTp
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.Dept ?= sess.paymentData.payment.Dbtr.PstlAdr.Dept
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.SubDept ?= sess.paymentData.payment.Dbtr.PstlAdr.SubDept
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.StrtNm ?= sess.paymentData.payment.Dbtr.PstlAdr.StrtNm
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.BldgNb ?= sess.paymentData.payment.Dbtr.PstlAdr.BldgNb
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.PstCd ?= sess.paymentData.payment.Dbtr.PstlAdr.PstCd
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.TwnNm ?= sess.paymentData.payment.Dbtr.PstlAdr.TwnNm
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.CtrySubDvsn ?= sess.paymentData.payment.Dbtr.PstlAdr.CtrySubDvsn
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.Ctry ?= sess.paymentData.payment.Dbtr.PstlAdr.Ctry
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.AdrLine[0] ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrLine[0]
ext.ReturnPaymentRequest.Document.PmtRtr.TxInf.OrgnlTxRef.Cdtr.PstlAdr.AdrLine[1] ?= sess.paymentData.payment.Dbtr.PstlAdr.AdrLine[1]   // drop if ext AdrLine allows only one line

// PAL-1653 Saving Transaction Status Code from Cuscal so it can be stored in IPF Translog
saved.settlementStatusData_payment_TxSts ?= sess.settlementStatusData.payment.TxSts

return true
