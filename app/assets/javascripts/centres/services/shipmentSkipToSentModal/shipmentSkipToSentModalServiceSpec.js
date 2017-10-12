/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('shipmentSkipToSentModalService', function() {

  beforeEach(() => {
    angular.mock.module('ngAnimateMock', 'biobankApp', 'biobank.test');
    angular.mock.inject(function(ModalTestSuiteMixin, TestUtils) {
      _.extend(this, ModalTestSuiteMixin);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$animate',
                              '$document',
                              'shipmentSkipToSentModalService',
                              'Factory');
      this.addModalMatchers();
      TestUtils.addCustomMatchers();

      this.openModal = () => {
        this.modal = this.shipmentSkipToSentModalService.open();
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
