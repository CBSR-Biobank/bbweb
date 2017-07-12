/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/admin/studies/components/annotationTypes/collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAdd.html',
    controller: CollectionEventAnnotationTypeAddController,
    controllerAs: 'vm',
    bindings: {
      study:               '<',
      collectionEventType: '<'
    }
  };

  CollectionEventAnnotationTypeAddController.$inject = [
    '$controller',
    '$state',
    'notificationsService',
    'domainNotificationService'
  ];

  /*
   * Controller for this component.
   */
  function CollectionEventAnnotationTypeAddController($controller,
                                                      $state,
                                                      notificationsService,
                                                      domainNotificationService) {
    var vm = this;
    vm.$onInit = onInit;

    //---

    function onInit() {
      vm.domainObjTypeName = 'Collection Event Type';
      vm.addAnnotationTypePromiseFunc = vm.collectionEventType.addAnnotationType.bind(vm.collectionEventType);
      vm.returnState = 'home.admin.studies.study.collection.ceventType';

      // initialize this controller's base class
      $controller('AnnotationTypeAddController', {
        vm:                        vm,
        $state:                    $state,
        notificationsService:      notificationsService,
        domainNotificationService: domainNotificationService
      });

      vm.init();
    }
  }

  return component;
});
