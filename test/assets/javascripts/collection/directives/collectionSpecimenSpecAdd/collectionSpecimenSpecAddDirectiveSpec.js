/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('collectionSpecimenSpecAddDirective', function() {

    var createDirective = function (test) {
      this.element = angular.element([
        '<collection-specimen-spec-add',
        ' study="vm.study"',
        ' collection-event-type="vm.collectionEventType">',
        '</collection-specimen-spec-add>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        study: this.study,
        collectionEventType: this.collectionEventType
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('collectionSpecimenSpecAdd');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'notificationsService',
                              'domainEntityService',
                              'Study',
                              'CollectionEventType',
                              'CollectionSpecimenSpec',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenSpecAdd/collectionSpecimenSpecAdd.html');

      self.jsonCevenType       = self.factory.collectionEventType();
      self.jsonStudy           = self.factory.defaultStudy();
      self.collectionEventType = new self.CollectionEventType(self.jsonCevenType);
      self.study               = new self.Study(self.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('has valid scope', function() {
      createDirective.call(this);

      expect(this.controller.study).toBe(this.study);
      expect(this.controller.collectionEventType).toBe(this.collectionEventType);

      expect(this.controller.submit).toBeFunction();
      expect(this.controller.cancel).toBeFunction();

      expect(this.controller.anatomicalSourceTypes).toBeDefined();
      expect(this.controller.preservTypes).toBeDefined();
      expect(this.controller.preservTempTypes).toBeDefined();
      expect(this.controller.specimenTypes).toBeDefined();
    });

    describe('on submit', function() {

      beforeEach(function() {
        this.jsonSpec     = this.factory.collectionSpecimenSpec();
        this.specimenSpec = new this.CollectionSpecimenSpec(this.jsonSpec);
      });


      it('can submit a specimen spec', function() {
        spyOn(this.CollectionEventType.prototype, 'addSpecimenSpec')
          .and.returnValue(this.$q.when(this.collectionEventType));
        spyOn(this.notificationsService, 'submitSuccess').and.callThrough();

        createDirective.call(this);
        this.controller.submit(this.specimenSpec);
        this.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.admin.studies.study.collection.ceventType', {}, { reload: true });
        expect(this.notificationsService.submitSuccess).toHaveBeenCalled();
      });

      it('displays an error when submit fails', function() {
        spyOn(this.CollectionEventType.prototype, 'addSpecimenSpec')
          .and.returnValue(this.$q.reject('simulated error'));
        spyOn(this.domainEntityService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));

        createDirective.call(this);
        this.controller.submit(this.specimenSpec);
        this.scope.$digest();

        expect(this.domainEntityService.updateErrorModal).toHaveBeenCalled();
      });

    });

    it('on cancel returns to correct state', function() {
      createDirective.call(this);
      this.controller.cancel();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType');
    });

  });

});
