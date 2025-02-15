import org.apache.ofbiz.entity.GenericEntityException
import org.apache.ofbiz.entity.util.EntityQuery
import org.apache.ofbiz.base.util.Debug


def findCustomerDetails(Map context) {
    if (context == null) {
        context = [:]
    }   
    
    String partyId = context.partyId
    List customerList = []
    String responseMessage = "Success"
    
    try {
        if (partyId) {
            customerList = EntityQuery.use(delegator)
                                      .from("FindCustomerView")
                                      .where("partyId", partyId)
                                      .queryList()
        } else {
            customerList = EntityQuery.use(delegator)
                                      .from("FindCustomerView")
                                      .queryList()
        }
    } catch (GenericEntityException e) {
        responseMessage = "Error retrieving customer details: " + e.getMessage()
        Debug.logError("Error in findCustomerDetails service: " + e, "customerFinder")
    }
    
    def result = [:]
    result.customerList = customerList
    result.responseMessage = responseMessage
    return result
}

