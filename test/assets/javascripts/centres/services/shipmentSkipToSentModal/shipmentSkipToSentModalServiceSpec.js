/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('shipmentSkipToSentModalService', function() {

    beforeEach(mocks.module('ngAnimateMock', 'biobankApp', 'biobank.test'));

    beforeEach(inject(function(ModalTestSuiteMixin, testUtils) {
      _.extend(this, ModalTestSuiteMixin.prototype);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$animate',
                              '$document',
                              'shipmentSkipToSentModalService',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/centres/services/shipmentSkipToSentModal/shipmentSkipToSentModal.html',
        '/assets/javascripts/common/components/dateTimePicker/dateTimePicker.html');

      this.addModalMatchers();
      testUtils.addCustomMatchers();

      this.openModal = function () {
        this.modal = this.shipmentSkipToSentModalService.open();
        this.$rootScope.$digest();
        this.modalElement = this.modalElementFind();
        this.scope = this.modalElement.scope();
      };
    }));

    it('can open modal', function() {
      this.openModal();
      expect(this.$document).toHaveModalsOpen(1);
      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('ok button can be pressed', function() {
      this.openModal();
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.vm.okPressed();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('cancel button can be pressed', function() {
      this.openModal();
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.vm.cancelPressed();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('can edit the time packed', function() {
      var timeNow = new Date();

      this.openModal();
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.vm.timePackedOnEdit(timeNow);
      expect(this.scope.vm.timePacked).toBe(timeNow);
      this.scope.vm.cancelPressed();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('can edit the time sent', function() {
      var timeNow = new Date();

      this.openModal();
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.vm.timeSentOnEdit(timeNow);
      expect(this.scope.vm.timeSent).toBe(timeNow);
      this.scope.vm.cancelPressed();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

});
