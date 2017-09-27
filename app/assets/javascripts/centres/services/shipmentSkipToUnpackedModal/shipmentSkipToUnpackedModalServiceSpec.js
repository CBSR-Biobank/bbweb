/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('shipmentSkipToUnpackedModalService', function() {

  beforeEach(() => {
    angular.mock.module('ngAnimateMock', 'biobankApp', 'biobank.test');
    angular.mock.inject(function(ModalTestSuiteMixin, testUtils) {
      _.extend(this, ModalTestSuiteMixin.prototype);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$animate',
                              '$document',
                              'shipmentSkipToUnpackedModalService',
                              'factory');
      this.addModalMatchers();
      testUtils.addCustomMatchers();

      this.openModal = () => {
        this.modal = this.shipmentSkipToUnpackedModalService.open();
        this.modal.result.then(function () {}, function () {});
        this.$rootScope.$digest();
        this.modalElement = this.modalElementFind();
        this.scope = this.modalElement.scope();
      };

    });
  });

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

  it('can edit the time received', function() {
    var timeNow = new Date();

    this.openModal();
    expect(this.$document).toHaveModalsOpen(1);
    this.scope.vm.timeReceivedOnEdit(timeNow);
    expect(this.scope.vm.timeReceived).toBe(timeNow);
    this.scope.vm.cancelPressed();
    this.flush();
    expect(this.$document).toHaveModalsOpen(0);
  });

  it('can edit the time unpacked', function() {
    var timeNow = new Date();

    this.openModal();
    expect(this.$document).toHaveModalsOpen(1);
    this.scope.vm.timeUnpackedOnEdit(timeNow);
    expect(this.scope.vm.timeUnpacked).toBe(timeNow);
    this.scope.vm.cancelPressed();
    this.flush();
    expect(this.$document).toHaveModalsOpen(0);
  });

});
