/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function collectionEventAnnotationTypeAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:               '=',
        collectionEventType: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/directives/annotationTypes/collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAdd.html',
      controller: CollectionEventAnnotationTypeAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CollectionEventAnnotationTypeAddCtrl.$inject = [
    'annotationTypeAddService'
  ];

  var returnState = 'home.admin.studies.study.collection.ceventType';

  function CollectionEventAnnotationTypeAddCtrl(annotationTypeAddService) {
    var vm = this;

    vm.onSubmit        = onSubmit;
    vm.onAddsuccessful = annotationTypeAddService.onAddSuccessful(returnState);
    vm.onCancel        = annotationTypeAddService.onCancel(returnState);

    //--

    function onSubmit(annotationType) {
      vm.collectionEventType.addAnnotationType(annotationType)
        .then(vm.onAddsuccessful).catch(annotationTypeAddService.addFailed);
    }
  }

  return collectionEventAnnotationTypeAddDirective;

});
