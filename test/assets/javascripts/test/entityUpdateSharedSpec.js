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
   * @param {string} context.modalInputFuncName the name of the ""modalInput" service function that is called
   * to ask the user for input.
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

      beforeEach(inject(function () {
        var self = this;

        self.$q         = self.$injector.get('$q');
        self.modalInput = self.$injector.get('modalInput');

        self.deferred = self.$q.defer();
        spyOn(self.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: self.deferred.promise });
      }));

      it('context should be valid', function() {
        expect(context.entity.prototype[context.updateFuncName]).toBeFunction();
        expect(this.modalInput[context.modalInputFuncName]).toBeFunction();
      });

      it('should update a field on the enitity', function() {
        this.deferred.resolve(context.newValue);
        spyOn(context.entity.prototype, context.updateFuncName).and.returnValue(this.$q.when(context.entity));

        context.createController.call(this);
        expect(this.controller[context.controllerFuncName]).toBeFunction();
        this.controller[context.controllerFuncName]();
        this.scope.$digest();
        expect(context.entity.prototype[context.updateFuncName])
          .toHaveBeenCalledWith(context.newValue);
        expect(this.modalInput[context.modalInputFuncName]).toHaveBeenCalled();
      });

      it('should display an error in a modal when update fails', function() {
        var newValue = this.factory.stringNext(),
            deferred = this.$q.defer();

        this.deferred.resolve(newValue);
        expect(context.entity.prototype[context.updateFuncName]).toBeFunction();

        spyOn(context.entity.prototype, context.updateFuncName).and.returnValue(deferred.promise);
        spyOn(this.notificationsService, 'updateError');

        deferred.reject('simulated error');

        context.createController.call(this);
        expect(this.controller[context.controllerFuncName]).toBeFunction();
        this.controller[context.controllerFuncName]();
        this.scope.$digest();
        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });

  }

  return entityUpdateSharedSpec;
});
