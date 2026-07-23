import React from 'react'
import SocialMediaIcons from './SocialMediaIcons'
import FooterInfo from './FooterInfo'

const Footer = () => {
  return (
    <footer className='bg-gradient-to-r from-[var(--primary-red)] via-[var(--secondary-dark-red)] to-[var(--primary-red)] text-white overflow-hidden'>      
        <div className='w-full mx-auto px-4 sm:px-6 lg:px-8 py-4 sm:py-6 flex items-center justify-center border-b border-white'>
            <SocialMediaIcons/>
        </div>
        <div className='w-full mx-auto px-4 sm:px-6 lg:px-8 py-4 sm:py-6 border-b border-white'>
            <FooterInfo/>
        </div>    
        <div className='flex items-center justify-center w-full py-4 sm:py-6 lg:py-8 px-4'>
            <p className='text-xs sm:text-sm text-center'>
                UnivGo © 2025. Proyecto desarrollado para la Universidad de Medellín.
            </p>
        </div>
        
    </footer>
  )
}

export default Footer