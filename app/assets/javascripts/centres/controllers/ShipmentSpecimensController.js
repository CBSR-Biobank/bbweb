/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Base controller for components that display shipment specimens.
 *
 * This controller is usually used as a base class.
 *
 * @memberOf centres.controllers
 */
class ShipmentSpecimensController {

  /**
   * @param {AngularJS_Service} $q
   *
   * @param {domain.centres.ShipmentSpecimen} ShipmentSpecimen - The AngularJS Factory.
   */
  constructor($q, ShipmentSpecimen) {
    /**
     * The shipment to retrieve specimens for.
     *
     * @name centres.controllers.ShipmentSpecimensController#shipment
     * @type {domain.centres.Shipment}
     */

    Object.assign(this, { $q, ShipmentSpecimen })
  }

  /**
   * Returns the specimens to associate with the shipment.
   *
   * Needs to return a promise.
   */
  getSpecimens(options) {
    if (!this.shipment) {
      return this.$q.when({});
    }

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
