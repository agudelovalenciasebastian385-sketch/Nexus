package nexusmarket.adapters.useCases;

import nexusmarket.domain.services.support.ServiceHarness;

/** Builds each use case over the real services of a ServiceHarness, in the order of its constructor. */
final class UseCaseFactory {

    private UseCaseFactory() {
    }

    static PublicAccessUseCaseImpl publicAccess(ServiceHarness h) {
        return new PublicAccessUseCaseImpl(h.registerBuyerUser, h.login, h.logout, h.consultCatalog);
    }

    static BuyerUseCaseImpl buyer(ServiceHarness h) {
        return new BuyerUseCaseImpl(h.consultUser, h.changeUserPassword, h.consultBuyer, h.updateBuyerProfile,
                h.addItemToCart, h.removeItemFromCart, h.updateCartItemQuantity, h.consultCart, h.clearCart,
                h.confirmOrder, h.validatePayment, h.consultOrder, h.consultBuyerOrders, h.consultInvoice,
                h.requestReturn, h.consultReturn);
    }

    static SellerUseCaseImpl seller(ServiceHarness h) {
        return new SellerUseCaseImpl(h.consultUser, h.changeUserPassword, h.consultSeller, h.updateSeller,
                h.registerWarehouse, h.consultWarehouse, h.updateWarehouse, h.registerProduct, h.updateProduct,
                h.publishProduct, h.suspendProduct, h.discontinueProduct, h.consultSellerProducts,
                h.registerInitialStock, h.registerInventoryInbound, h.consultInventory, h.consultOrder,
                h.sellerPerformanceReport);
    }

    static LogisticsOperatorUseCaseImpl logisticsOperator(ServiceHarness h) {
        return new LogisticsOperatorUseCaseImpl(h.consultUser, h.changeUserPassword, h.consultWarehouse,
                h.registerInitialStock, h.registerInventoryInbound, h.registerInventoryAdjustment, h.reserveInventory,
                h.releaseInventoryReservation, h.consultInventory, h.consultOrder, h.createShipments, h.packShipment,
                h.dispatchShipment, h.confirmDelivery, h.inventoryReport);
    }

    static AdministratorUseCaseImpl administrator(ServiceHarness h) {
        return new AdministratorUseCaseImpl(h.registerStaffUser, h.consultUser, h.changeUserStatus,
                h.changeUserPassword, h.registerSeller, h.consultSeller, h.updateSeller, h.consultBuyer,
                h.changeBuyerCommercialStatus, h.registerWarehouse, h.consultWarehouse, h.updateWarehouse,
                h.consultSellerProducts, h.consultInvoice, h.approveReturn, h.rejectReturn, h.processRefund,
                h.consultReturn, h.consultAuditLogs, h.commercialReport, h.sellerPerformanceReport, h.inventoryReport,
                h.logisticsReport);
    }

    static SupervisorUseCaseImpl supervisor(ServiceHarness h) {
        return new SupervisorUseCaseImpl(h.consultUser, h.changeUserPassword, h.consultSeller, h.consultBuyer,
                h.consultWarehouse, h.consultSellerProducts, h.consultInventory, h.consultCart, h.consultOrder,
                h.consultBuyerOrders, h.consultInvoice, h.consultReturn, h.consultAuditLogs, h.commercialReport,
                h.sellerPerformanceReport, h.inventoryReport, h.logisticsReport);
    }
}
