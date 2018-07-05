/**
 * AngularJS Component for {@link domain.centres.Centre Centre} administration.
 *
 * @namespace admin.centres.components.centreLocationView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';
import angular from 'angular';

/*
 * Controller for this component.
 */
class CentreLocationViewController {

  constructor($state,
              gettextCatalog,
              modalInput,
              notificationsService,
              domainNotificationService,
              breadcrumbService) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    modalInput,
                    notificationsService,
                    domainNotificationService,
                    breadcrumbService
                  });
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.centres'),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.centres.centre.locations({ centreId: "${this.centre.id}", locationId: "${this.location.id}" })`,
        () => this.centre.name),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.centres.centre.locations.locationsView',
        () => this.location.name)
    ];
  }

  back() {
    this.$state.go('home.admin.centres.centre.locations', {}, { reload: true });
  }

  postUpdate(message, title, timeout = 1500) {
    return (centre) => {
      this.centre = centre;
      this.location = _.find(this.centre.locations, { id: this.location.id });
      this.notificationsService.success(message, title, timeout);
    };
  }

  editName() {
    this.modalInput.text(this.gettextCatalog.getString('Edit location name'),
                         this.gettextCatalog.getString('Name'),
                         this.location.name,
                         { required: true, minLength: 2 }).result
      .then(name => {
        this.location.name = name;
        return this.centre.updateLocation(this.location)
      })
      .then(this.postUpdate(this.gettextCatalog.getString('Name changed successfully.'),
                            this.gettextCatalog.getString('Change successful')))
      .catch(error => {
        this.notificationsService.updateError(error);
      })
      .then(() => {
        // reload the state so that the URL gets updated
        this.$state.go(this.$state.current.name,
                       { locationSlug: this.location.slug },
                       { reload: true  })
      });
  }

  editStreet() {
    this.modalInput.text(this.gettextCatalog.getString('Edit street address'),
                         this.gettextCatalog.getString('Street address'),
                         this.location.street).result
      .then(street => {
        this.location.street = street;
        return this.centre.updateLocation(this.location);
      })
      .then(this.postUpdate(this.gettextCatalog.getString('Street address changed successfully.'),
                            this.gettextCatalog.getString('Change successful')))
      .catch(error => {
        this.notificationsService.updateError(error);
      });
  }

  editCity() {
    this.modalInput.text(this.gettextCatalog.getString('Edit city name'),
                         this.gettextCatalog.getString('City'),
                         this.location.city).result
      .then(city => {
        this.location.city = city;
        return this.centre.updateLocation(this.location);
      })
      .then(this.postUpdate(this.gettextCatalog.getString('City changed successfully.'),
                            this.gettextCatalog.getString('Change successful')))
      .catch(error => {
        this.notificationsService.updateError(error);
      });
  }

  editProvince() {
    this.modalInput.text(this.gettextCatalog.getString('Edit province'),
                         this.gettextCatalog.getString('Province'),
                         this.location.province).result
      .then(province => {
        this.location.province = province;
        return this.centre.updateLocation(this.location);
      })
      .then(this.postUpdate(this.gettextCatalog.getString('Province changed successfully.'),
                            this.gettextCatalog.getString('Change successful')))
      .catch(error => {
        this.notificationsService.updateError(error);
      });
  }

  editPostalCode() {
    this.modalInput.text(this.gettextCatalog.getString('Edit postal code'),
                         this.gettextCatalog.getString('Postal code'),
                         this.location.postalCode).result
      .then(postalCode => {
        this.location.postalCode = postalCode;
        return this.centre.updateLocation(this.location);
      })
      .then(this.postUpdate(this.gettextCatalog.getString('PostalCode changed successfully.'),
                            this.gettextCatalog.getString('Change successful')))
      .catch(error => {
        this.notificationsService.updateError(error);
      });
  }

  editPoBoxNumber() {
    this.modalInput.text(this.gettextCatalog.getString('Edit PO box number'),
                         this.gettextCatalog.getString('PO box Number'),
                         this.location.poBoxNumber).result
      .then(poBoxNumber => {
        this.location.poBoxNumber = poBoxNumber;
        return this.centre.updateLocation(this.location);
      })
      .then(this.postUpdate(this.gettextCatalog.getString('PoBoxNumber changed successfully.'),
                            this.gettextCatalog.getString('Change successful')))
      .catch(error => {
        this.notificationsService.updateError(error);
      });
  }

  editCountryIsoCode() {
    this.modalInput.text(this.gettextCatalog.getString('Edit country ISO code'),
                         this.gettextCatalog.getString('Country ISO code'),
                         this.location.countryIsoCode).result
      .then(countryIsoCode => {
        this.location.countryIsoCode = countryIsoCode;
        return this.centre.updateLocation(this.location);
      })
      .then(this.postUpdate(this.gettextCatalog.getString('CountryIsoCode changed successfully.'),
                            this.gettextCatalog.getString('Change successful')))
      .catch(error => {
        this.notificationsService.updateError(error);
      });
  }

  remove() {
    const doRemove = () => this.centre.removeLocation(this.location)
          .then(centre => {
            this.centre = centre;
          });

    this.domainNotificationService
      .removeEntity(doRemove,
                    this.gettextCatalog.getString('Remove Location'),
                    this.gettextCatalog.getString('Are you sure you want to remove location {{name}}?',
                                                  { name: this.location.name}),
                    this.gettextCatalog.getString('Remove Failed'),
                    this.gettextCatalog.getString('Location {{name}} cannot be removed: ',
                                                  { name: this.location.name}))
      .then(() => this.back())
      .catch(angular.noop);
  }

}

/**
 * An AngularJS component that allows the user to view a {@link domain.Location Location} associated with a
 * {@link domain.centres.Centre Centre}.
 *
 * @memberOf admin.centres.components.centreLocationView
 *
 * @param {domain.centres.Centre} centre - the centre to add the location to.
 *
 * @param {domain.Location} location - the location to view.
 */
const centreLocationViewComponent = {
  template: require('./centreLocationView.html'),
  controller: CentreLocationViewController,
  controllerAs: 'vm',
  bindings: {
    centre:   '<',
    location: '<'
  }
};

export default ngModule => ngModule.component('centreLocationView', centreLocationViewComponent)
