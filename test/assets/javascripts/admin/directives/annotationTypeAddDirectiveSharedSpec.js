/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  /**
   *
   * @param {function } context.createController is a function that creates the controller and scope:
   * this.controller, and this.scope.
   *
   * @param {object} context.entity is the domain entity to which the annotation type will be added to.
   *
   * @param {string} context.addAnnotationTypeFuncName is the name of the function on context.entity which
   * adds the annotation type.
   *
   * @param {string} context.returnState the state to return after a successful add or when cancel is pressed.
   */
  function annotationTypeAddDirectiveSharedSpec(context) {

    describe('(shared) tests', function() {

      beforeEach(inject(function() {
        this.$state         = this.$injector.get('$state');
        this.AnnotationType = this.$injector.get('AnnotationType');
        this.factory   = this.$injector.get('factory');

        spyOn(this.$state, 'go').and.returnValue('ok');
      }));

      it('should change to correct state on submit', function() {
        var $q        = this.$injector.get('$q'),
            annotType = new this.AnnotationType(this.factory.annotationType());

        spyOn(context.entity.prototype, context.addAnnotationTypeFuncName)
          .and.returnValue($q.when(this.study));

        context.createController();

        this.controller.onSubmit(annotType);
        this.scope.$digest();
        expect(context.entity.prototype[context.addAnnotationTypeFuncName])
          .toHaveBeenCalledWith(annotType);
        expect(this.$state.go).toHaveBeenCalledWith(
          context.returnState, {}, { reload: true });
      });

      it('on cancel the correct method should be called', function() {
        context.createController();
        this.controller.onCancel();
        this.scope.$digest();
        expect(this.$state.go).toHaveBeenCalledWith(context.returnState);
      });

    });
  }

  return annotationTypeAddDirectiveSharedSpec;

});
