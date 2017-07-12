/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('shipmentAddItemsComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin, testUtils) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentAddItems/shipmentAddItems.html',
        '/assets/javascripts/common/components/progressTracker/progressTracker.html',
        '/assets/javascripts/centres/components/shippingInfoView/shippingInfoView.html',
        '/assets/javascripts/centres/components/shipmentSpecimensAdd/shipmentSpecimensAdd.html',
        '/assets/javascripts/common/components/collapsiblePanel/collapsiblePanel.html',
        '/assets/javascripts/shipmentSpecimens/components/ssSpecimensPagedTable/ssSpecimensPagedTable.html',
        '/assets/javascripts/common/components/statusLine/statusLine.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'toastr',
                              'Shipment',
                              'SHIPMENT_SEND_PROGRESS_ITEMS',
                              'modalInput',
                              'modalService',
                              'shipmentSkipToSentModalService',
                              'domainNotificationService',
                              'notificationsService',
                              'factory');
      testUtils.addCustomMatchers();

      this.createController = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<shipment-add-items shipment="vm.shipment"></shipment-add-items',
          { shipment: shipment },
          'shipmentAddItems');
      };
    }));

    it('should have valid scope', function() {
      var shipment = this.createShipment();
      this.createController(shipment);
      expect(this.controller.shipment).toBe(shipment);
      expect(this.controller.progressInfo).toBeDefined();
      expect(this.controller.progressInfo.items).toBeArrayOfSize(this.SHIPMENT_SEND_PROGRESS_ITEMS.length);
      expect(this.controller.progressInfo.items).toContainAll(this.SHIPMENT_SEND_PROGRESS_ITEMS);
      expect(this.controller.progressInfo.current).toBe(2);
      expect(this.controller.tagAsPacked).toBeFunction();
      expect(this.controller.tagAsSent).toBeFunction();
      expect(this.controller.removeShipment).toBeFunction();
    });

    describe('can change state to packed on shipment', function() {

      beforeEach(function() {
        this.shipment = this.createShipmentWithSpecimens(1);

        spyOn(this.Shipment, 'get').and.returnValue(this.$q.when(this.shipment));
        spyOn(this.$state, 'go').and.returnValue(null);
      });

      it('can tag a shipment as packed', function() {
        var self = this,
            promiseSuccess;

        spyOn(this.Shipment.prototype, 'pack').and.returnValue(this.$q.when(this.shipment));
        spyOn(this.modalInput, 'dateTime').and.returnValue({ result: this.$q.when(new Date()) });

        this.createController(this.shipment);
        this.controller.tagAsPacked().then(function () {
          expect(self.Shipment.prototype.pack).toHaveBeenCalled();
          expect(self.$state.go).toHaveBeenCalledWith('home.shipping.shipment',
                                                      { shipmentId: self.shipment.id});
          promiseSuccess = true;
        });
        this.scope.$digest();
        expect(promiseSuccess).toBeTrue();
      });

    });

    describe('when tagging as sent', function() {

      beforeEach(function() {
        this.shipment = this.createShipmentWithSpecimens(1);
        spyOn(this.Shipment, 'get').and.returnValue(this.$q.when(this.shipment));
        spyOn(this.shipmentSkipToSentModalService, 'open').and
          .returnValue({
            result: this.$q.when({
              timePacked: new Date(),
              timeSent: new Date()
            })
          });
      });

      it('can tag a shipment as sent', function() {
        var self = this,
            promiseSuccess;

        spyOn(this.$state, 'go').and.returnValue(null);
        spyOn(this.Shipment.prototype, 'skipToStateSent').and.returnValue(this.$q.when(this.shipment));

        this.createController(this.shipment);
        this.controller.tagAsSent().then(function () {
          expect(self.Shipment.prototype.skipToStateSent).toHaveBeenCalled();
          expect(self.$state.go).toHaveBeenCalledWith('home.shipping.shipment',
                                                      { shipmentId: self.shipment.id },
                                                      { reload: true });
          promiseSuccess = true;
        });
        this.scope.$digest();
        expect(promiseSuccess).toBeTrue();
      });

      it('user is informed if shipment cannot be tagged as sent', function() {
        var self = this,
            errorMsgs = [
              'TimeSentBeforePacked',
              'simulated error'
            ];

        spyOn(this.toastr, 'error').and.returnValue(null);
        this.createController(this.shipment);
        errorMsgs.forEach(function (errMsg, index) {
          var args;

          self.Shipment.prototype.skipToStateSent =
            jasmine.createSpy().and.returnValue(self.$q.reject({ message: errMsg }));

          self.controller.tagAsSent();
          self.scope.$digest();
          expect(self.toastr.error.calls.count()).toBe(index + 1);

          if (errMsg === 'TimeReceivedBeforeSent') {
            args = self.toastr.error.calls.argsFor(index);
            expect(args[0]).toContain('The received time is before the sent time');
          }
        });
      });

    });

    describe('not allowed to change state', function() {

      beforeEach(function() {
        this.shipment = this.createShipmentWithSpecimens(0);
        spyOn(this.Shipment, 'get').and.returnValue(this.$q.when(this.shipment));
        spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));
        this.createController(this.shipment);
      });

      it('to packed when no specimens in shipment', function() {
        var self = this,
            promiseFailed;

        this.controller.tagAsPacked().catch(function () {
          expect(self.modalService.modalOk).toHaveBeenCalled();
          promiseFailed = true;
        });
        this.scope.$digest();
        expect(promiseFailed).toBeTrue();
      });

      it('to sent when no specimens in shipment', function() {
        var self = this,
            promiseFailed;

        this.controller.tagAsSent().catch(function () {
          expect(self.modalService.modalOk).toHaveBeenCalled();
          promiseFailed = true;
        });
        this.scope.$digest();
        expect(promiseFailed).toBeTrue();
      });

    });

    it('can remove a shipment', function() {
      var shipment = this.createShipment();

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.Shipment.prototype, 'remove').and.returnValue(this.$q.when(true));
      spyOn(this.notificationsService, 'success').and.returnValue(null);
      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController(shipment);
      this.controller.removeShipment();
      this.scope.$digest();

      expect(this.Shipment.prototype.remove).toHaveBeenCalled();
      expect(this.notificationsService.success).toHaveBeenCalled();
      expect(this.$state.go).toHaveBeenCalledWith('home.shipping');
    });

    it('removal of a shipment can be cancelled', function() {
      var shipment = this.createShipment();

      spyOn(this.Shipment.prototype, 'remove').and.returnValue(this.$q.when(true));

      this.createController(shipment);
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.reject('Cancel'));
      this.controller.removeShipment();
      this.scope.$digest();

      expect(this.Shipment.prototype.remove).not.toHaveBeenCalled();
    });

    it('removeShipment does nothing if shipment is not defined', function() {
      var shipment = this.createShipment();

      spyOn(this.Shipment.prototype, 'remove').and.returnValue(this.$q.when(true));

      this.createController(shipment);
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.reject('Cancel'));
      this.controller.removeShipment();
      this.scope.$digest();

      expect(this.Shipment.prototype.remove).not.toHaveBeenCalled();
    });

  });

});
