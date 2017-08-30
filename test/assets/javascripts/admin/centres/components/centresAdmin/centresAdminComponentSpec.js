/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Component: centresAdmin', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<centres-admin></centres-admin>',
          undefined,
          'centresAdmin');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Centre',
                              'CentreCounts',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/admin/centres/components/centresAdmin/centresAdmin.html',
        '/assets/javascripts/admin/centres/components/centresPagedList/centresPagedList.html',
        '/assets/javascripts/common/components/nameAndStateFilters/nameAndStateFilters.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html',
        '/assets/javascripts/common/components/debouncedTextInput/debouncedTextInput.html');
    }));

    it('scope is valid on startup', function() {
      spyOn(this.CentreCounts, 'get').and.returnValue(this.$q.when({ total: 0, disabled: 0, enabled: 0 }));
      spyOn(this.Centre, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([])));
      this.createController();
      expect(this.scope).toBeDefined();
    });

  });

});
