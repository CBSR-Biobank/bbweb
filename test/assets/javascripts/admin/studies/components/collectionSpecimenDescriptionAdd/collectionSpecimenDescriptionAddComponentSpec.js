/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Component: collectionSpecimenDescriptionAdd', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<collection-specimen-description-add',
            ' study="vm.study"',
            ' collection-event-type="vm.collectionEventType">',
            '</collection-specimen-description-add>'
          ].join(''),
          {
            study: this.study,
            collectionEventType: this.collectionEventType
          },
          'collectionSpecimenDescriptionAdd');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'notificationsService',
                              'domainNotificationService',
                              'Study',
                              'CollectionEventType',
                              'CollectionSpecimenDescription',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/collectionSpecimenDescriptionAdd/collectionSpecimenDescriptionAdd.html');

      this.jsonCevenType       = this.factory.collectionEventType();
      this.jsonStudy           = this.factory.defaultStudy();
      this.collectionEventType = new this.CollectionEventType(this.jsonCevenType);
      this.study               = new this.Study(this.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('has valid scope', function() {
      this.createController();

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
        this.jsonSpec     = this.factory.collectionSpecimenDescription();
        this.specimenDescription = new this.CollectionSpecimenDescription(this.jsonSpec);
      });


      it('can submit a specimen spec', function() {
        spyOn(this.CollectionEventType.prototype, 'addSpecimenDescription')
          .and.returnValue(this.$q.when(this.collectionEventType));
        spyOn(this.notificationsService, 'submitSuccess').and.callThrough();

        this.createController();
        this.controller.submit(this.specimenDescription);
        this.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.admin.studies.study.collection.ceventType', {}, { reload: true });
        expect(this.notificationsService.submitSuccess).toHaveBeenCalled();
      });

      it('displays an error when submit fails', function() {
        spyOn(this.CollectionEventType.prototype, 'addSpecimenDescription')
          .and.returnValue(this.$q.reject('simulated error'));
        spyOn(this.domainNotificationService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));

        this.createController();
        this.controller.submit(this.specimenDescription);
        this.scope.$digest();

        expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
      });

    });

    it('on cancel returns to correct state', function() {
      this.createController();
      this.controller.cancel();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType');
    });

  });

});
