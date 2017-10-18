/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * Controller for components that display shipment specimens.
 *
 * This controller is usually used as a base class.
 *
 * @param {domain.centres.Shipment} vm.shipment - the shipment to retrieve specimens for.
 */
/* @ngInject */
function ShipmentSpecimenController(vm,
                                    $q,
                                    ShipmentSpecimen) {
  vm.getSpecimens = getSpecimens;

  //---

  /**
   * Returns the specimens to associated with the shipment.
   *
   * Needs to return a promise.
   */
  function getSpecimens(options) {
    if (!vm.shipment) { return $q.when({}); }

    return ShipmentSpecimen.list(vm.shipment.id, options)
      .then(function (paginatedResult) {
        return { items: paginatedResult.items, maxPages: paginatedResult.maxPages };
      });
  }

}

export default ngModule => ngModule.controller('ShipmentSpecimensController', ShipmentSpecimenController)
