/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  /**
   * @param {object} context.entity the entity to be update.
   *
   * @param {string} context.updateFuncName the name of the function that does the update.
   *
   * @param {string} context.controllerFuncname the name of the function on the controller to invoke.
   *
   * @param {string} context.modalServiceFuncName the name of the modal function that is called to ask the
   * user for input.
   *
   * @param {any} context.newValue the new value to assign.
   *
   * @param {function} this.createController a function that creates the controller.
   *
   * @param {object} this.controller the controller.
   *
   * @param {object} this.scope the scope bound to the controller.
   */
  function entityUpdateSharedSpec(context) {

    describe('update functions', function () {

      it('context should be valid', function() {
        expect(context.entity.prototype[context.updateFuncName]).toBeFunction();
        expect(this.modalService[context.modalServiceFuncName]).toBeFunction();
      });

      it('should update a field on the enitity', function() {
        spyOn(this.modalService, context.modalServiceFuncName)
          .and.returnValue(this.$q.when(context.newValue));
        spyOn(context.entity.prototype, context.updateFuncName).and.returnValue(this.$q.when(context.entity));

        this.createController();
        expect(this.controller[context.controllerFuncName]).toBeFunction();
        this.controller[context.controllerFuncName]();
        this.scope.$digest();
        expect(context.entity.prototype[context.updateFuncName])
          .toHaveBeenCalledWith(context.newValue);
        expect(this.modalService[context.modalServiceFuncName]).toHaveBeenCalled();
      });

      it('should display an error in a modal when update fails', function() {
        var newValue = this.jsonEntities.stringNext(),
            deferred = this.$q.defer();

        expect(context.entity.prototype[context.updateFuncName]).toBeFunction();

        spyOn(this.modalService, context.modalServiceFuncName).and.returnValue(this.$q.when(newValue));
        spyOn(context.entity.prototype, context.updateFuncName).and.returnValue(deferred.promise);
        spyOn(this.notificationsService, 'updateError');

        deferred.reject('simulated error');

        this.createController();
        expect(this.controller[context.controllerFuncName]).toBeFunction();
        this.controller[context.controllerFuncName]();
        this.scope.$digest();
        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });

  }

  return entityUpdateSharedSpec;
});
