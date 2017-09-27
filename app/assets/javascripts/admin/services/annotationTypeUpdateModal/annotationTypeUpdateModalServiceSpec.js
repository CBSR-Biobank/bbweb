/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('annotationTypeUpdateModalService', function() {

  beforeEach(() => {
    angular.mock.module('ngAnimateMock', 'biobankApp', 'biobank.test');
    angular.mock.inject(function(ModalTestSuiteMixin, testUtils) {
      _.extend(this, ModalTestSuiteMixin.prototype);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$animate',
                              '$document',
                              'AnnotationType',
                              'AnnotationValueType',
                              'annotationTypeUpdateModal',
                              'factory');

      this.addModalMatchers();
      testUtils.addCustomMatchers();

      this.openModal = (annotationType) => {
        this.modal = this.annotationTypeUpdateModal.openModal(annotationType);
        this.modal.result.then(() => {}, function () {});
        this.$rootScope.$digest();
        this.modalElement = this.modalElementFind();
        this.scope = this.modalElement.scope();
      };

      this.createAnnotationType = (valueType) =>
        new this.AnnotationType(this.factory.annotationType({ valueType: valueType }));

      this.createSelectAnnotationType = () =>
        this.createAnnotationType(this.AnnotationValueType.SELECT);
    });
  });

  it('can open modal', function() {
    var annotationType = this.createSelectAnnotationType();

    this.openModal(annotationType);
    expect(this.$document).toHaveModalsOpen(1);
    this.dismiss();
    expect(this.$document).toHaveModalsOpen(0);
  });

  it('using a non select annotation throws an exception', function() {
    var invalidValueTypes = [
          this.AnnotationValueType.TEXT,
          this.AnnotationValueType.NUMBER,
          this.AnnotationValueType.DATE_TIME
        ];

    invalidValueTypes.forEach((valueType) => {
      var annotationType = this.createAnnotationType(valueType);

      expect(() => {
        this.openModal(annotationType);
      }).toThrowError(/invalid annotation type/);
    });
  });

  it('ok button can be pressed', function() {
    var annotationType = this.createSelectAnnotationType();

    this.openModal(annotationType);
    expect(this.$document).toHaveModalsOpen(1);
    this.scope.vm.okPressed();
    this.flush();
    expect(this.$document).toHaveModalsOpen(0);
  });

  it('cancel button can be pressed', function() {
    var annotationType = this.createSelectAnnotationType();

    this.openModal(annotationType);
    expect(this.$document).toHaveModalsOpen(1);
    this.scope.vm.closePressed();
    this.flush();
    expect(this.$document).toHaveModalsOpen(0);
  });

  describe('for remove button', function() {

    it('remove button is disabled', function() {
      this.annotationType = this.createSelectAnnotationType();
      this.annotationType.options = [ 1, 2, 3 ];
      this.openModal(this.annotationType);
      expect(this.$document).toHaveModalsOpen(1);
      expect(this.scope.vm.removeButtonDisabled()).toBeFalse();
      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('remove button is disabled for "select" annotation type', function() {
      this.annotationType = this.createSelectAnnotationType();
      this.annotationType.options = [];
      this.openModal(this.annotationType);
      expect(this.$document).toHaveModalsOpen(1);
      expect(this.scope.vm.removeButtonDisabled()).toBeTrue();
      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  it('can add an option', function() {
    this.annotationType = this.createSelectAnnotationType();
    this.annotationType.options = [];
    this.openModal(this.annotationType);
    expect(this.$document).toHaveModalsOpen(1);
    this.scope.vm.optionAdd();
    expect(this.scope.vm.options).toBeArrayOfSize(1);
    this.dismiss();
    expect(this.$document).toHaveModalsOpen(0);
  });

  describe('removing options', function() {

    beforeEach(function() {
      this.annotationType = this.createSelectAnnotationType();
      this.annotationType.options = [ 1, 2, 3 ];
      this.openModal(this.annotationType);
      expect(this.$document).toHaveModalsOpen(1);
    });


    it('remove button is disabled', function() {
      this.scope.vm.optionRemove(0);
      this.scope.vm.optionRemove(0);
      expect(this.scope.vm.options).toBeArrayOfSize(this.annotationType.options.length - 2);

      expect(() => {
        this.scope.vm.optionRemove(0);
      }).toThrowError(/options is empty/);

      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

  });
});
