/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  AnnotationTypeAddController.$inject = [
    'vm',
    '$state',
    'notificationsService',
    'domainNotificationService'
  ];

  /**
   * Base class controller for components that allow adding annotation types.
   *
   * @param {object} vm - The derived controller object.
   *
   * @param {string} vm.domainObjTypeName - The name of the domain entity this annotation type belongs to.
   *
   * @param {function} vm.addAnnotationTypePromiseFunc - The funtion that will add the annotation type to the
   * domain entity.
   *
   * @param {string} vm.returnState - The state to return to when either the submit or cancel buttons are pressed.
   *
   * @param {object} $state - The UI Router state object.
   *
   * @param {service} notificationsService - The AngularJS service.
   *
   * @param {service} domainNotificationService - The AngularJS service.
   *
   * @return {void} nothing.
   */
  function AnnotationTypeAddController(vm,
                                       $state,
                                       notificationsService,
                                       domainNotificationService) {

    vm.init = init;

    //-------

    function init() {
      vm.submit = submit;
      vm.cancel = cancel;
    }

    function submit(annotationType) {
      vm.addAnnotationTypePromiseFunc(annotationType)
        .then(function () {
          notificationsService.submitSuccess();
          $state.go(vm.returnState, {}, { reload: true });
        })
        .catch(function (error) {
          return domainNotificationService.updateErrorModal(error, vm.domainObjTypeName);
        });
    }

    function cancel() {
      $state.go(vm.returnState, {}, { reload: true });
    }

  }

  return AnnotationTypeAddController;
});
