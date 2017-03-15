/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Used for controllers that derive from ShipmentSpecimensControler.
   *
   * @param {object} context the parameters for the shared behaviour.
   */
  function sharedBehaviour() {

    describe('can get specimens from server', function() {

      beforeEach(function() {
        this.injectDependencies('ShipmentSpecimen');
      });

      it('valid results when shipment is valid', function() {
        var shipment = this.createShipment();

        spyOn(this.ShipmentSpecimen, 'list')
          .and.returnValue(this.$q.when(this.factory.pagedResult([], { maxPages: 1 })));

        this.createScope(shipment);
        this.controller.getSpecimens({}).then(function (result) {
          expect(result).toBeObject();
          expect(result.items).toBeEmptyArray();
          expect(result.maxPages).toBe(1);
        });
        this.scope.$digest();

        expect(this.ShipmentSpecimen.list).toHaveBeenCalledWith(shipment.id, {});
      });

      it('valid results if shipment is invalid', function() {
        this.createScope();
        this.controller.getSpecimens({}).then(function (result) {
          expect(result).toBeObject();
        });
        this.scope.$digest();
      });

    });
  }

  return sharedBehaviour;

});
