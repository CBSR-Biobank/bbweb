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
   * @param {object} context.entity is the domain entity to which the annotation type will be added to.
   *
   * @param {string} context.updateAnnotationTypeFuncName is the name of the function on context.entity which
   * adds the annotation type.
   *
   * @param {object} context.parentObject the object that holds the annotation type.
   *
   * @param {AnnotationType} context.annotationType the annotation type to be viewed.
   *
   * @param {function } context.createController is a function that creates the controller and scope:
   * this.controller, and this.scope.
   *
   * NOTE: needs that the test suite be extended with testSuiteMixin.
   */
  function annotationTypeViewDirectiveSharedSpec(context) {

    describe('(shared) tests', function() {

      beforeEach(inject(function() {
        this.injectDependencies('AnnotationType', 'factory');
      }));

      it('on update should invoke the update method on entity', function() {
        spyOn(context.entity.prototype, context.updateAnnotationTypeFuncName)
          .and.returnValue(this.$q.when(context.parentObject));
        spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

        context.createController.call(this);
        this.controller.onUpdate(context.annotationType);
        this.scope.$digest();
        expect(context.entity.prototype[context.updateAnnotationTypeFuncName])
          .toHaveBeenCalledWith(context.annotationType);
        expect(this.notificationsService.success).toHaveBeenCalled();
      });

      it('error message should be displayed when update fails', function() {
        var deferred = this.$q.defer();

        spyOn(context.entity.prototype, context.updateAnnotationTypeFuncName)
          .and.returnValue(deferred.promise);
        spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));
        deferred.reject('simulated error');

        context.createController.call(this);
        this.controller.onUpdate(context.annotationType);
        this.scope.$digest();
        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

      it('throws an error if the post update fails', function() {
        // entity is missing the annotation type
        var obj = new context.entity(context.jsonObject);

        spyOn(context.entity.prototype, context.updateAnnotationTypeFuncName)
          .and.returnValue(this.$q.when(obj));
        spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));

        context.createController.call(this);
        this.controller.onUpdate(context.annotationType);
        this.scope.$digest();

        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });
  }

  return annotationTypeViewDirectiveSharedSpec;

});
