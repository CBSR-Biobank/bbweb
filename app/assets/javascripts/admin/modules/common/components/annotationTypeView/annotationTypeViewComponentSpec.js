/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: annotationTypeView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Study',
                              'AnnotationType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'annotationTypeUpdateModal',
                              'modalInput',
                              'notificationsService',
                              'Factory');

      this.returnState      = 'my-return-state';
      this.study            = new this.Study(this.Factory.study());
      this.annotationType   = new this.AnnotationType(this.Factory.annotationType());
      this.onUpdate         = jasmine.createSpy('onUpdate').and.returnValue(this.$q.when(this.study));

      this.createController = ({ readOnly = false, annotationType = this.annotationType } = {}) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          [
            '<annotation-type-view ',
            '  annotation-type="vm.annotationType"',
            '  read-only="vm.readOnly"',
            '  on-update="vm.onUpdate"',
            '</annotation-type-view>',
          ].join(''),
          {
            annotationType: annotationType,
            readOnly:       readOnly,
            onUpdate:       this.onUpdate
          },
          'annotationTypeView');
      };

    });
  });

  it('should have valid scope', function() {
    const readOnly = false;
    this.createController({ readOnly });
    expect(this.controller.annotationType).toBe(this.annotationType);
    expect(this.controller.readOnly).toBe(readOnly);
    expect(this.controller.onUpdate).toBeFunction();
  });

  it('call to back function returns to valid state', function() {
    spyOn(this.$state, 'go').and.returnValue(null);

    this.createController();
    this.controller.back();
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('^', {}, { reload: true });
  });

  describe('updates to name', function () {

    var context = {};

    beforeEach(function () {
      context.attribute = 'name';
      context.controllerFuncName = 'editName';
      context.modalInputFuncName = 'text';
    });

    sharedUpdateBehaviour(context);

  });

  describe('updates to description', function () {

    var context = {};

    beforeEach(function () {
      context.attribute = 'description';
      context.controllerFuncName = 'editDescription';
      context.modalInputFuncName = 'textArea';
    });

    sharedUpdateBehaviour(context);

  });

  describe('updates to required', function () {

    var context = {};

    beforeEach(function () {
      context.attribute = 'required';
      context.controllerFuncName = 'editRequired';
      context.modalInputFuncName = 'boolean';
    });

    sharedUpdateBehaviour(context);

  });

  describe('updates to selections', function () {

    it('can edit selections on a single select', function () {
      var annotationType = new this.AnnotationType(
        this.Factory.annotationType({
          valueType:     this.AnnotationValueType.SELECT,
          maxValueCount: this.AnnotationMaxValueCount.SELECT_SINGLE,
          options:       [ 'option1', 'option2' ],
          required:      true
        }));
      this.annotationTypeUpdateModal.openModal = jasmine.createSpy()
        .and.returnValue({ result: this.$q.when([]) });

      this.createController({ study: undefined, annotationType: annotationType });
      this.controller.editSelectionOptions();
      this.scope.$digest();
      expect(this.annotationTypeUpdateModal.openModal).toHaveBeenCalled();
    });

    it('an exception is thrown for annotation types that are not select', function () {
      var self = this,
          annotationTypes = _.chain(self.Factory.allAnnotationTypes())
          .filter(function (json) {
            return (json.valueType !== self.AnnotationValueType.SELECT);
          })
          .map(function (json) {
            var result = new self.AnnotationType(json);
            return result;
          })
          .value();

      annotationTypes.forEach((annotationType) => {
        expect(function () {
          self.createController({ study: undefined, annotationType: annotationType });
          self.controller.editSelectionOptions();
        }).toThrowError(/invalid annotation type:/);
      });
    });

  });

  function sharedUpdateBehaviour(context) {

    describe('(shared) update functions', function () {

      it('should update a field', function() {
        var newValue = this.Factory.stringNext();

        spyOn(this.modalInput, context.modalInputFuncName).and.returnValue(
          { result: this.$q.when(newValue)});

        this.createController();
        expect(this.controller[context.controllerFuncName]).toBeFunction();
        this.controller[context.controllerFuncName]();
        this.scope.$digest();
        expect(this.onUpdate).toHaveBeenCalled();
        expect(this.onUpdate.calls.argsFor(0)[0]).toBe(context.attribute);
      });

    });
  }

});
