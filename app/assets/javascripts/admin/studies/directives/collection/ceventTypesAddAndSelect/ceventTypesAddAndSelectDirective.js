/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
   'use strict';

   /**
    * Description
    */
   function ceventTypesAddAndSelectDirective() {
      var directive = {
         restrict: 'E',
         scope: {},
         bindToController: {
            study: '='
         },
         templateUrl : '/assets/javascripts/admin/studies/directives/collection/ceventTypesAddAndSelect/ceventTypesAddAndSelect.html',
         controller: CeventTypesAddAndSelectCtrl,
         controllerAs: 'vm'
      };

      return directive;
   }

   CeventTypesAddAndSelectCtrl.$inject = [
     '$state',
     'gettextCatalog',
     'CollectionEventType'
   ];

   function CeventTypesAddAndSelectCtrl($state,
                                        gettextCatalog,
                                        CollectionEventType) {
      var vm = this;

      vm.collectionEventTypes = [];
      vm.showPagination       = false;
      vm.add                  = add;
      vm.select               = select;
      vm.getRecurringLabel    = getRecurringLabel;

      init();

      //--

      function init() {
         CollectionEventType.list(vm.study.id).then(function (list) {
            vm.collectionEventTypes = list;
         });
      }

      function add() {
         $state.go('home.admin.studies.study.collection.ceventTypeAdd');
      }

      function select(ceventType) {
         $state.go('home.admin.studies.study.collection.ceventType', { ceventTypeId: ceventType.id });
      }

      function getRecurringLabel(ceventType) {
         return ceventType.recurring ?
            gettextCatalog.getString('Recurring') : gettextCatalog.getString('Not recurring');
      }
   }

   return ceventTypesAddAndSelectDirective;

});
