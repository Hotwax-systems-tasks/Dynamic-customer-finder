import org.apache.ofbiz.entity.Delegator
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.service.LocalDispatcher
import org.apache.ofbiz.service.ServiceUtil

def createCustomer(Map context) {
    Delegator delegator = context.Delegator
    if (!delegator) {
        return ServiceUtil.returnError("Delegator not found in context")
    }
    String emailAddress = context.emailAddress
    String firstName = context.firstName
    String lastName = context.lastName

    if (!emailAddress || !firstName || !lastName) {
        return ServiceUtil.returnError("Missing required fields: emailAddress, firstName, lastName")
    }

    // Check if customer already exists
    EntityCondition condition = EntityCondition.makeCondition("emailAddress", EntityOperator.EQUALS, emailAddress)
    GenericValue existingCustomer = delegator.findOne("FindCustomerView", condition, false)

    if (existingCustomer) {
        return ServiceUtil.returnError("Customer with email ${emailAddress} already exists")
    }

    // Create new customer record
    GenericValue newParty = delegator.makeValue("Party")
    newParty.set("partyId", delegator.getNextSeqId("Party"))
    newParty.set("partyTypeId", "PERSON")
    newParty.create()

    GenericValue newPerson = delegator.makeValue("Person")
    newPerson.set("partyId", newParty.partyId)
    newPerson.set("firstName", firstName)
    newPerson.set("lastName", lastName)
    newPerson.create()

    GenericValue newContactMech = delegator.makeValue("ContactMech")
    newContactMech.set("contactMechId", delegator.getNextSeqId("ContactMech"))
    newContactMech.set("contactMechTypeId", "EMAIL_ADDRESS")
    newContactMech.set("infoString", emailAddress)
    newContactMech.create()

    GenericValue newPartyContactMech = delegator.makeValue("PartyContactMech")
    newPartyContactMech.set("partyId", newParty.partyId)
    newPartyContactMech.set("contactMechId", newContactMech.contactMechId)
    newPartyContactMech.create()

    return ServiceUtil.returnSuccess("Customer created successfully with partyId: ${newParty.partyId}")
}
