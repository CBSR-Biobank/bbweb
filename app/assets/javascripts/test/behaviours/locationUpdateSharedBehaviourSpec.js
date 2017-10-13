/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function() {
  'use strict';

  /**
   * Used for directives that update locations on a domain entity.
   */
  function locationUpdateSharedBehaviour(context) {

    describe('(shared)', function() {

      beforeEach(function() {
        this.injectDependencies('$q', 'modalInput', 'notificationsService');
      });

      it('on update should invoke the update method on entity', function() {
        var modalInputDeferred = this.$q.defer();

        modalInputDeferred.resolve(context.newValue);

        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: modalInputDeferred.promise});
        spyOn(context.entity, context.entityUpdateFuncName)
          .and.returnValue(this.$q.when(context.entity));
        spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

        context.createController.call(this);
        this.controller[context.controllerUpdateFuncName](context.location);
        this.scope.$digest();

        expect(context.entity[context.entityUpdateFuncName]).toHaveBeenCalled();
        expect(this.controller.location).toBeDefined();
        expect(this.notificationsService.success).toHaveBeenCalled();
      });

      it('error message should be displayed when update fails', function() {
        spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));
        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: this.$q.when(context.newValue) });

        context.createController.call(this);

        spyOn(context.entity, context.entityUpdateFuncName)
          .and.returnValue(this.$q.reject('simulated error'));

        this.controller[context.controllerUpdateFuncName](context.annotation);
        this.scope.$digest();

        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });

  }

  return locationUpdateSharedBehaviour;
});
