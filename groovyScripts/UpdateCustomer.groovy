import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityCondition

/**
 * Service: updateCustomer
 * Required Input: emailAddress
 * Optional Inputs: postalAddress, contactNumber
 * Output: resultMessage (String)
 */
def updateCustomer(Map context) {
    if (context == null) context = [:]
    ExecutionContext ec = context.ec
    
    // Retrieve required and optional input parameters
    def emailAddress = context.emailAddress
    def newPostalAddress = context.postalAddress
    def newContactNumber = context.contactNumber
    
    // Use the findCustomer service to locate the customer by email address.
    def findResult = ec.service.sync("findCustomer", [ emailAddress: emailAddress ])
    if (!findResult.customerList || findResult.customerList.size() == 0) {
         return [ resultMessage: "Customer with email ${emailAddress} not found." ]
    }
    
    // Assume the first matching record is the customer to update.
    def customer = findResult.customerList.get(0)
    def partyId = customer.partyId
    
    // Update the postal address if provided.
    if (newPostalAddress) {
        // Try to find an existing postal address record.
        def pcmPostalList = ec.entity.find("partyEntities.party.PartyContactMechPurpose")
            .condition("partyId", partyId)
            .condition("contactMechPurposeTypeId", "PostalAddressPrimary")
            .list()
        if (pcmPostalList && pcmPostalList.size() > 0) {
            def pcmPostal = pcmPostalList.get(0)
            def contactMechId = pcmPostal.contactMechId
            // Update the ContactMech record for the postal address.
            def cmPostal = ec.entity.find("partyEntities.party.ContactMech")
                .condition("contactMechId", contactMechId)
                .one()
            if (cmPostal) {
                cmPostal.set("infoString", newPostalAddress)
                cmPostal.update()
            }
        } else {
            // If no postal address record exists, create one.
            def pcmPostal = ec.entity.makeValue("partyEntities.party.PartyContactMechPurpose")
            pcmPostal.partyId = partyId
            pcmPostal.contactMechPurposeTypeId = "PostalAddressPrimary"
            def cmPostal = ec.entity.makeValue("partyEntities.party.ContactMech")
            cmPostal.contactMechId = ec.entity.getNextSeqId("ContactMech")
            cmPostal.infoString = newPostalAddress
            cmPostal.create()
            pcmPostal.contactMechId = cmPostal.contactMechId
            pcmPostal.create()
        }
    }
    
    // Update the contact number if provided.
    if (newContactNumber) {
        // Try to find an existing phone number record.
        def pcmPhoneList = ec.entity.find("partyEntities.party.PartyContactMechPurpose")
            .condition("partyId", partyId)
            .condition("contactMechPurposeTypeId", "PhonePrimary")
            .list()
        if (pcmPhoneList && pcmPhoneList.size() > 0) {
            def pcmPhone = pcmPhoneList.get(0)
            def contactMechId = pcmPhone.contactMechId
            def cmPhone = ec.entity.find("partyEntities.party.ContactMech")
                .condition("contactMechId", contactMechId)
                .one()
            if (cmPhone) {
                cmPhone.set("infoString", newContactNumber)
                cmPhone.update()
            }
        } else {
            // If no phone record exists, create one.
            def pcmPhone = ec.entity.makeValue("partyEntities.party.PartyContactMechPurpose")
            pcmPhone.partyId = partyId
            pcmPhone.contactMechPurposeTypeId = "PhonePrimary"
            def cmPhone = ec.entity.makeValue("partyEntities.party.ContactMech")
            cmPhone.contactMechId = ec.entity.getNextSeqId("ContactMech")
            cmPhone.infoString = newContactNumber
            cmPhone.create()
            pcmPhone.contactMechId = cmPhone.contactMechId
            pcmPhone.create()
        }
    }
    
    return [ resultMessage: "Customer updated successfully." ]
}
