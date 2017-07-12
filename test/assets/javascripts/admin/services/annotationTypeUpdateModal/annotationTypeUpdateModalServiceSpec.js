/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('annotationTypeUpdateModalService', function() {

    function SuiteMixinFactory(ModalTestSuiteMixin) {

      function SuiteMixin() {
        ModalTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ModalTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.openModal = function (annotationType) {
        this.modal = this.annotationTypeUpdateModal.openModal(annotationType);
        this.modal.result.then(function () {}, function () {});
        this.$rootScope.$digest();
        this.modalElement = this.modalElementFind();
        this.scope = this.modalElement.scope();
      };

      SuiteMixin.prototype.createAnnotationType = function (valueType) {
        return new this.AnnotationType(this.factory.annotationType({ valueType: valueType }));
      };

      SuiteMixin.prototype.createSelectAnnotationType = function () {
        return this.createAnnotationType(this.AnnotationValueType.SELECT);
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('ngAnimateMock', 'biobankApp', 'biobank.test'));

    beforeEach(inject(function(ModalTestSuiteMixin, testUtils) {
      _.extend(this, new SuiteMixinFactory(ModalTestSuiteMixin).prototype);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$animate',
                              '$document',
                              'AnnotationType',
                              'AnnotationValueType',
                              'annotationTypeUpdateModal',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/admin/services/annotationTypeUpdateModal/annotationTypeUpdateModal.html');

      this.addModalMatchers();
      testUtils.addCustomMatchers();
    }));

    it('can open modal', function() {
      var annotationType = this.createSelectAnnotationType();

      this.openModal(annotationType);
      expect(this.$document).toHaveModalsOpen(1);
      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('using a non select annotation throws an exception', function() {
      var self = this,
          invalidValueTypes = [
            this.AnnotationValueType.TEXT,
            this.AnnotationValueType.NUMBER,
            this.AnnotationValueType.DATE_TIME
          ];

      invalidValueTypes.forEach(function (valueType) {
        var annotationType = self.createAnnotationType(valueType);

        expect(function () {
          self.openModal(annotationType);
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

      it('remove button is disabled', function() {
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
        var self = this;

        this.scope.vm.optionRemove(0);
        this.scope.vm.optionRemove(0);
        expect(this.scope.vm.options).toBeArrayOfSize(this.annotationType.options.length - 2);

        expect(function () {
          self.scope.vm.optionRemove(0);
        }).toThrowError(/options is empty/);

        this.dismiss();
        expect(this.$document).toHaveModalsOpen(0);
      });

    });

  });

});
