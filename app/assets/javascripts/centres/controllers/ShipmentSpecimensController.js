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
class ShipmentSpecimensController {

  constructor($q, ShipmentSpecimen) {
    Object.assign(this, { $q, ShipmentSpecimen })
  }

  /**
   * Returns the specimens to associated with the shipment.
   *
   * Needs to return a promise.
   */
  getSpecimens(options) {
    if (!this.shipment) { return this.$q.when({}); }

    return this.ShipmentSpecimen.list(this.shipment.id, options)
      .then(paginatedResult => ({
        items:    paginatedResult.items,
        maxPages: paginatedResult.maxPages
      }));
  }

}

// this controller does not need to be included in AngularJS since it is imported by the controllers that
// extend it, see ShipmentSpecimensController for example.
export { ShipmentSpecimensController }
export default () => {}
