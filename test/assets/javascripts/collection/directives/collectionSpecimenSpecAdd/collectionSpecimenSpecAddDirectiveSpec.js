/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('collectionSpecimenSpecAddDirective', function() {

    function createDirective(test) {
      var element,
          scope = test.$rootScope.$new();

      element = angular.element([
        '<collection-specimen-spec-add',
        ' study="vm.study"',
        ' collection-event-type="vm.collectionEventType">',
        '</collection-specimen-spec-add>'
      ].join(''));

      scope.vm = {
        study: test.study,
        collectionEventType: test.collectionEventType
      };
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('collectionSpecimenSpecAdd')
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q                     = self.$injector.get('$q');
      self.$rootScope             = self.$injector.get('$rootScope');
      self.$compile               = self.$injector.get('$compile');
      self.$state                 = self.$injector.get('$state');
      self.notificationsService   = self.$injector.get('notificationsService');
      self.domainEntityService    = self.$injector.get('domainEntityService');
      self.Study                  = self.$injector.get('Study');
      self.CollectionEventType    = self.$injector.get('CollectionEventType');
      self.CollectionSpecimenSpec = self.$injector.get('CollectionSpecimenSpec');
      self.jsonEntities           = self.$injector.get('jsonEntities');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/collection/collectionSpecimenSpecAdd/collectionSpecimenSpecAdd.html');

      self.jsonCevenType       = self.jsonEntities.collectionEventType();
      self.jsonStudy           = self.jsonEntities.defaultStudy();
      self.collectionEventType = new self.CollectionEventType(self.jsonCevenType);
      self.study               = new self.Study(self.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);

      expect(directive.controller.study).toBe(this.study);
      expect(directive.controller.collectionEventType).toBe(this.collectionEventType);

      expect(directive.controller.submit).toBeFunction();
      expect(directive.controller.cancel).toBeFunction();

       expect(directive.controller.anatomicalSourceTypes).toBeDefined();
       expect(directive.controller.preservTypes).toBeDefined();
       expect(directive.controller.preservTempTypes).toBeDefined();
       expect(directive.controller.specimenTypes).toBeDefined();
    });

    describe('on submit', function() {

      beforeEach(function() {
        this.jsonSpec     = this.jsonEntities.collectionSpecimenSpec();
        this.specimenSpec = new this.CollectionSpecimenSpec(this.jsonSpec);
      });


      it('can submit a specimen spec', function() {
        var directive;

        spyOn(this.CollectionEventType.prototype, 'addSpecimenSpec')
          .and.returnValue(this.$q.when(this.collectionEventType));
        spyOn(this.notificationsService, 'submitSuccess').and.callThrough();

        directive = createDirective(this);
        directive.controller.submit(this.specimenSpec);
        directive.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.admin.studies.study.collection.ceventType', {}, { reload: true });
        expect(this.notificationsService.submitSuccess).toHaveBeenCalled();
      });

      it('displays an error when submit fails', function() {
        var directive,
            deferred = this.$q.defer();

        deferred.reject('simulated update error');
        spyOn(this.CollectionEventType.prototype, 'addSpecimenSpec')
          .and.returnValue(deferred.promise);
        spyOn(this.domainEntityService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));

        directive = createDirective(this);
        directive.controller.submit(this.specimenSpec);
        directive.scope.$digest();

        expect(this.domainEntityService.updateErrorModal).toHaveBeenCalled();
      });

    });

    it('on cancel returns to correct state', function() {
      var directive;

      directive = createDirective(this);
      directive.controller.cancel();
      directive.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType');
    });


  });

});
